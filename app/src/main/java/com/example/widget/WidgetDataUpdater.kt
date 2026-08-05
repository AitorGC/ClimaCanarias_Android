package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AirQualityData
import com.example.data.CalimaSeverity
import com.example.data.WeatherCondition
import com.example.data.WeatherDomainData
import com.example.db.FavoriteBeach
import com.example.data.AemetWarningDomainData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WidgetDataUpdater {

    private const val PREFS_NAME = "climacanarias_widgets_prefs"

    // 1. Save Weather Summary
    fun saveWeatherSummary(context: Context, data: WeatherDomainData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("weather_city", data.cityName)
            .putFloat("weather_temp", data.temperatureCelsius.toFloat())
            .putFloat("weather_humidity", data.humidity.toFloat())
            .putFloat("weather_wind", data.windSpeedKmh.toFloat())
            .putString("weather_condition", getConditionLabel(data.condition))
            .putString("weather_icon", getConditionEmoji(data.condition))
            .putFloat("weather_max", data.dailyForecast.firstOrNull()?.maxTemp?.toFloat() ?: (data.temperatureCelsius + 2).toFloat())
            .putFloat("weather_min", data.dailyForecast.firstOrNull()?.minTemp?.toFloat() ?: (data.temperatureCelsius - 4).toFloat())
            .putLong("weather_time", System.currentTimeMillis())
            .apply()

        data.airQuality?.let { saveAirQuality(context, data.cityName, it) }

        updateWeatherWidget(context)
        updateAirQualityWidget(context)
    }

    // 2. Save Air Quality & Calima
    fun saveAirQuality(context: Context, cityName: String, aqi: AirQualityData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("aqi_city", cityName)
            .putFloat("aqi_pm25", aqi.pm25.toFloat())
            .putFloat("aqi_pm10", aqi.pm10.toFloat())
            .putInt("aqi_index", aqi.europeanAqi)
            .putString("aqi_calima", aqi.calimaSeverity.name)
            .putString("aqi_alert", aqi.calimaAlertMessage ?: "")
            .apply()

        updateAirQualityWidget(context)
    }

    // 3. Save AEMET Warning
    fun saveAemetWarnings(context: Context, warnings: List<AemetWarningDomainData>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val highest = warnings.firstOrNull { it.nivel.equals("Rojo", ignoreCase = true) }
            ?: warnings.firstOrNull { it.nivel.equals("Naranja", ignoreCase = true) }
            ?: warnings.firstOrNull { it.nivel.equals("Amarillo", ignoreCase = true) }
            ?: warnings.firstOrNull()

        if (highest != null) {
            prefs.edit()
                .putString("alert_level", highest.nivel ?: "Verde")
                .putString("alert_phenomenon", highest.fenomeno ?: "Condiciones Normales")
                .putString("alert_zone", highest.ambitoGeografico ?: "Todas las Islas")
                .putString("alert_desc", highest.descripcion ?: "Sin riesgo meteorológico significativo.")
                .apply()
        } else {
            prefs.edit()
                .putString("alert_level", "Verde")
                .putString("alert_phenomenon", "Sin Riesgo")
                .putString("alert_zone", "Islas Canarias")
                .putString("alert_desc", "No hay avisos meteorológicos activos de AEMET.")
                .apply()
        }

        updateAemetAlertsWidget(context)
    }

    // 4. Save Marine / Beach Info
    fun saveFavoriteBeachInfo(context: Context, beach: FavoriteBeach, waveHeight: Double, waterTemp: Double, flag: String, seaState: String, windSpeed: Double, windDir: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("beach_name", beach.name)
            .putFloat("beach_wave", waveHeight.toFloat())
            .putFloat("beach_water_temp", waterTemp.toFloat())
            .putString("beach_flag", flag)
            .putString("beach_sea_state", seaState)
            .putFloat("beach_wind_speed", windSpeed.toFloat())
            .putString("beach_wind_dir", windDir)
            .apply()

        updateMarineWidget(context)
    }

    // UPDATE METHODS FOR EACH WIDGET

    fun updateWeatherWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, WeatherWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
        if (appWidgetIds.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val city = prefs.getString("weather_city", "Las Palmas de GC") ?: "Las Palmas de GC"
        val temp = prefs.getFloat("weather_temp", 23.5f)
        val humidity = prefs.getFloat("weather_humidity", 65f)
        val wind = prefs.getFloat("weather_wind", 22f)
        val condition = prefs.getString("weather_condition", "Soleado") ?: "Soleado"
        val icon = prefs.getString("weather_icon", "☀️") ?: "☀️"
        val maxTemp = prefs.getFloat("weather_max", 26f)
        val minTemp = prefs.getFloat("weather_min", 19f)

        val views = RemoteViews(context.packageName, R.layout.widget_weather_summary)
        views.setTextViewText(R.id.tv_city_name, city)
        views.setTextViewText(R.id.tv_temperature, "${temp.toInt()}°")
        views.setTextViewText(R.id.tv_condition, condition)
        views.setTextViewText(R.id.tv_feels_like, "Sensación: ${(temp + 0.8f).toInt()}°C")
        views.setTextViewText(R.id.tv_weather_icon, icon)
        views.setTextViewText(R.id.tv_temp_range, "Máx ${maxTemp.toInt()}° • Mín ${minTemp.toInt()}°")
        views.setTextViewText(R.id.tv_humidity_wind, "💧 ${humidity.toInt()}% • 💨 ${wind.toInt()} km/h")

        // Set pending intent to open tab 0
        views.setOnClickPendingIntent(R.id.widget_container, createTabPendingIntent(context, 0, 101))

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    fun updateAemetAlertsWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, AemetAlertsWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
        if (appWidgetIds.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val level = prefs.getString("alert_level", "Verde") ?: "Verde"
        val phenomenon = prefs.getString("alert_phenomenon", "Sin Riesgo") ?: "Sin Riesgo"
        val zone = prefs.getString("alert_zone", "Islas Canarias") ?: "Islas Canarias"
        val desc = prefs.getString("alert_desc", "Sin avisos meteorológicos activos.") ?: "Sin avisos activos."

        val views = RemoteViews(context.packageName, R.layout.widget_aemet_alerts)

        val (bgDrawable, textColorStr, badgeText) = when (level.lowercase()) {
            "rojo" -> Triple(R.drawable.widget_background_alert_red, "#F87171", "AVISO ROJO")
            "naranja" -> Triple(R.drawable.widget_background_alert_orange, "#FB923C", "AVISO NARANJA")
            "amarillo" -> Triple(R.drawable.widget_background_alert_yellow, "#FACC15", "AVISO AMARILLO")
            else -> Triple(R.drawable.widget_background_alert_green, "#34D399", "SIN AVISOS")
        }

        views.setInt(R.id.widget_container, "setBackgroundResource", bgDrawable)
        views.setTextViewText(R.id.tv_alert_badge, badgeText)
        views.setTextViewText(R.id.tv_island_zone, "📍 $zone")
        views.setTextViewText(R.id.tv_alert_phenomenon, "⚠️ $phenomenon: $desc")

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.tv_updated_time, "Actualizado: $timeStr")

        // Pending intent to open tab 2 (Alertas)
        views.setOnClickPendingIntent(R.id.widget_container, createTabPendingIntent(context, 2, 102))

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    fun updateMarineWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, MarineWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
        if (appWidgetIds.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val beachName = prefs.getString("beach_name", "Playa de Las Canteras") ?: "Playa de Las Canteras"
        val wave = prefs.getFloat("beach_wave", 1.2f)
        val waterTemp = prefs.getFloat("beach_water_temp", 21.5f)
        val flag = prefs.getString("beach_flag", "🟢 Bandera Verde") ?: "🟢 Bandera Verde"
        val seaState = prefs.getString("beach_sea_state", "Maredilla • Oleaje moderado") ?: "Maredilla"
        val windSpeed = prefs.getFloat("beach_wind_speed", 18f)
        val windDir = prefs.getString("beach_wind_dir", "NNE (Alisio)") ?: "NNE"

        val views = RemoteViews(context.packageName, R.layout.widget_marine)
        views.setTextViewText(R.id.tv_beach_name, "🏖️ $beachName")
        views.setTextViewText(R.id.tv_bathing_flag, flag)
        views.setTextViewText(R.id.tv_wave_height, "🌊 Olas: ${String.format(Locale.US, "%.1f", wave)} m")
        views.setTextViewText(R.id.tv_water_temp, "🌡️ Agua: ${String.format(Locale.US, "%.1f", waterTemp)}°C")
        views.setTextViewText(R.id.tv_sea_state, seaState)
        views.setTextViewText(R.id.tv_wind_info, "💨 Viento\n${windSpeed.toInt()} km/h\n$windDir")

        // Pending intent to open tab 1 (Playas)
        views.setOnClickPendingIntent(R.id.widget_container, createTabPendingIntent(context, 1, 103))

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    fun updateAirQualityWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, AirQualityWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
        if (appWidgetIds.isEmpty()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val city = prefs.getString("aqi_city", "Las Palmas de GC") ?: "Canarias"
        val pm25 = prefs.getFloat("aqi_pm25", 8.5f)
        val pm10 = prefs.getFloat("aqi_pm10", 15.0f)
        val aqiIndex = prefs.getInt("aqi_index", 1)
        val calima = prefs.getString("aqi_calima", "NONE") ?: "NONE"
        val alertMsg = prefs.getString("aqi_alert", "") ?: ""

        val (statusText, badgeText, emoji) = when {
            calima == "SEVERE" || pm10 > 100 -> Triple("Índice ICA: Calima Intensa 🌫️", "DESFAVORABLE", "🌫️")
            calima == "MODERATE" || pm10 > 50 -> Triple("Índice ICA: Calima Moderada", "REGULAR", "🌾")
            aqiIndex >= 3 -> Triple("Índice ICA: Calima o Polvo", "REGULAR", "🌤️")
            else -> Triple("Índice ICA: Buena (Excelente)", "BUENA", "🍃")
        }

        val recText = if (alertMsg.isNotEmpty()) alertMsg else if (calima != "NONE") "Usar mascarilla al aire libre si se es sensible." else "Ideal para realizar deportes al aire libre."

        val views = RemoteViews(context.packageName, R.layout.widget_air_quality)
        views.setTextViewText(R.id.tv_aqi_title, "🌪️ Aire y Calima • $city")
        views.setTextViewText(R.id.tv_aqi_badge, badgeText)
        views.setTextViewText(R.id.tv_aqi_status, statusText)
        views.setTextViewText(R.id.tv_particles_info, "PM2.5: ${String.format(Locale.US, "%.1f", pm25)} µg/m³ • PM10: ${String.format(Locale.US, "%.1f", pm10)} µg/m³")
        views.setTextViewText(R.id.tv_aqi_recommendation, recText)
        views.setTextViewText(R.id.tv_calima_icon, emoji)
        views.setTextViewText(R.id.tv_station_name, "Estación: Red Canaria Calidad del Aire")

        // Pending intent to open tab 3 (Estaciones/Aire)
        views.setOnClickPendingIntent(R.id.widget_container, createTabPendingIntent(context, 3, 104))

        appWidgetManager.updateAppWidget(appWidgetIds, views)
    }

    fun updateAllWidgets(context: Context) {
        updateWeatherWidget(context)
        updateAemetAlertsWidget(context)
        updateMarineWidget(context)
        updateAirQualityWidget(context)
    }

    private fun createTabPendingIntent(context: Context, targetTab: Int, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_TAB", targetTab)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getConditionLabel(condition: WeatherCondition): String {
        return when (condition) {
            WeatherCondition.SUNNY -> "Despejado / Soleado"
            WeatherCondition.CLOUDY -> "Nuboso / Intervalos"
            WeatherCondition.RAINY -> "Lluvia / Chubascos"
            WeatherCondition.SNOWY -> "Nieve en cumbres"
            WeatherCondition.STORM -> "Tormenta"
            WeatherCondition.CALIMA -> "Calima / Polvo en suspensión"
        }
    }

    private fun getConditionEmoji(condition: WeatherCondition): String {
        return when (condition) {
            WeatherCondition.SUNNY -> "☀️"
            WeatherCondition.CLOUDY -> "⛅"
            WeatherCondition.RAINY -> "🌧️"
            WeatherCondition.SNOWY -> "❄️"
            WeatherCondition.STORM -> "🌩️"
            WeatherCondition.CALIMA -> "🌾"
        }
    }
}
