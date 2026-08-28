package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val current: CurrentWeather?,
    val hourly: HourlyWeather?,
    val daily: DailyWeather? = null
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String>,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    val sunrise: List<String>?,
    val sunset: List<String>?,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Double?,
    @Json(name = "wind_speed_10m") val windSpeed: Double,
    @Json(name = "wind_direction_10m") val windDirection: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "uv_index") val uvIndex: Double?
)

@JsonClass(generateAdapter = true)
data class HourlyWeather(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "relative_humidity_2m") val humidity: List<Double>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>,
    @Json(name = "wind_speed_10m") val windSpeed: List<Double>?,
    @Json(name = "wind_direction_10m") val windDirection: List<Double>?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAqiResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentAqi?
)

@JsonClass(generateAdapter = true)
data class CurrentAqi(
    val time: String,
    @Json(name = "pm2_5") val pm25: Double?,
    val pm10: Double?,
    @Json(name = "nitrogen_dioxide") val no2: Double?,
    @Json(name = "ozone") val o3: Double?,
    @Json(name = "sulphur_dioxide") val so2: Double?
)

// Domain representations for unified State
enum class CalimaSeverity {
    NONE,
    LOW,
    MODERATE,
    SEVERE
}

enum class WeatherCondition {
    SUNNY,
    CLOUDY,
    RAINY,
    SNOWY,
    STORM,
    CALIMA
}

data class AirQualityData(
    val pm25: Double,
    val pm10: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val canaryAqiLevel: CanaryAqiLevel,
    val calimaSeverity: CalimaSeverity,
    val calimaAlertMessage: String?
)

enum class CanaryAqiLevel(
    val color: Long,
    val title: String,
    val generalRecommendation: String,
    val sensitiveRecommendation: String
) {
    BUENA(
        0xFF00FFFF,
        "Buena",
        "Disfruta de tus actividades al aire libre de manera normal.",
        "Disfruta de tus actividades al aire libre de manera normal."
    ),
    RAZONABLEMENTE_BUENA(
        0xFF4CAF50,
        "Razonablemente buena",
        "Disfruta de tus actividades al aire libre de manera normal.",
        "Disfruta de tus actividades al aire libre de manera normal."
    ),
    REGULAR(
        0xFFFFEB3B,
        "Regular",
        "Disfruta de tus actividades al aire libre de manera normal. Sin embargo, vigila la aparición de síntomas como tos, irritación de la garganta, falta de aire, fatiga excesiva o palpitaciones.",
        "Considera reducir las actividades prolongadas y enérgicas al aire libre. Las personas con asma o enfermedades respiratorias deben seguir cuidadosamente su plan de medicación."
    ),
    DESFAVORABLE(
        0xFFF44336,
        "Desfavorable",
        "Considera reducir las actividades prolongadas y enérgicas al aire libre, especialmente si experimentas tos, falta de aire o irritación de garganta.",
        "Considera reducir las actividades al aire libre, y realizarlas en el interior o posponerlas para cuando la calidad del aire sea buena o razonablemente buena."
    ),
    MUY_DESFAVORABLE(
        0xFFB71C1C,
        "Muy desfavorable",
        "Considera reducir las actividades al aire libre, y realizarlas en el interior o posponerlas para cuando la calidad del aire sea buena o razonablemente buena.",
        "Reduce toda actividad al aire libre, y considera realizar las actividades en el interior o posponerlas para cuando la calidad del aire sea buena o razonablemente buena."
    ),
    EXTREMADAMENTE_DESFAVORABLE(
        0xFF9C27B0,
        "Extremadamente desfavorable",
        "Reduce toda actividad al aire libre y considera realizar las actividades en el interior o posponerlas para cuando la calidad del aire sea buena o razonablemente buena.",
        "Evita la estancia prolongada al aire libre. Sigue el plan de tratamiento médico, en su caso, meticulosamente, y acude a un servicio de urgencias si tu estado de salud empeora."
    )
}

fun calculateCanaryAqiLevel(so2: Double, no2: Double, pm25: Double, pm10: Double, o3: Double): CanaryAqiLevel {
    val so2Level = when {
        so2 <= 100 -> CanaryAqiLevel.BUENA
        so2 <= 200 -> CanaryAqiLevel.RAZONABLEMENTE_BUENA
        so2 <= 350 -> CanaryAqiLevel.REGULAR
        so2 <= 500 -> CanaryAqiLevel.DESFAVORABLE
        so2 <= 750 -> CanaryAqiLevel.MUY_DESFAVORABLE
        else -> CanaryAqiLevel.EXTREMADAMENTE_DESFAVORABLE
    }
    val no2Level = when {
        no2 <= 40 -> CanaryAqiLevel.BUENA
        no2 <= 90 -> CanaryAqiLevel.RAZONABLEMENTE_BUENA
        no2 <= 120 -> CanaryAqiLevel.REGULAR
        no2 <= 230 -> CanaryAqiLevel.DESFAVORABLE
        no2 <= 340 -> CanaryAqiLevel.MUY_DESFAVORABLE
        else -> CanaryAqiLevel.EXTREMADAMENTE_DESFAVORABLE
    }
    val pm25Level = when {
        pm25 <= 10 -> CanaryAqiLevel.BUENA
        pm25 <= 20 -> CanaryAqiLevel.RAZONABLEMENTE_BUENA
        pm25 <= 25 -> CanaryAqiLevel.REGULAR
        pm25 <= 50 -> CanaryAqiLevel.DESFAVORABLE
        pm25 <= 75 -> CanaryAqiLevel.MUY_DESFAVORABLE
        else -> CanaryAqiLevel.EXTREMADAMENTE_DESFAVORABLE
    }
    val pm10Level = when {
        pm10 <= 20 -> CanaryAqiLevel.BUENA
        pm10 <= 40 -> CanaryAqiLevel.RAZONABLEMENTE_BUENA
        pm10 <= 50 -> CanaryAqiLevel.REGULAR
        pm10 <= 100 -> CanaryAqiLevel.DESFAVORABLE
        pm10 <= 150 -> CanaryAqiLevel.MUY_DESFAVORABLE
        else -> CanaryAqiLevel.EXTREMADAMENTE_DESFAVORABLE
    }
    val o3Level = when {
        o3 <= 50 -> CanaryAqiLevel.BUENA
        o3 <= 100 -> CanaryAqiLevel.RAZONABLEMENTE_BUENA
        o3 <= 130 -> CanaryAqiLevel.REGULAR
        o3 <= 240 -> CanaryAqiLevel.DESFAVORABLE
        o3 <= 380 -> CanaryAqiLevel.MUY_DESFAVORABLE
        else -> CanaryAqiLevel.EXTREMADAMENTE_DESFAVORABLE
    }
    
    val levels = listOf(so2Level, no2Level, pm25Level, pm10Level, o3Level)
    return levels.maxByOrNull { it.ordinal } ?: CanaryAqiLevel.BUENA
}

data class HourlyForecastItem(
    val timeString: String, // e.g. "12:00"
    val temperature: Double,
    val humidity: Double,
    val precipitationProbability: Int
)

data class DailyForecastItem(
    val dateString: String, // e.g. "Lunes", "Martes", "2026-05-28"
    val maxTemp: Double,
    val minTemp: Double,
    val precipitationProbability: Int,
    val condition: WeatherCondition,
    val weatherCode: Int,
    val uvIndexMax: Double? = null
)

data class WeatherDomainData(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val temperatureCelsius: Double,
    val humidity: Double,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Double,
    val condition: WeatherCondition,
    val weatherCode: Int,
    val uvIndex: Double?,
    val airQuality: AirQualityData?,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem> = emptyList(),
    val sunrise: String? = null,
    val sunset: String? = null,
    val isSynthetic: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class AemetStationDomainData(
    val indicativo: String,
    val nombre: String,
    val provincia: String,
    val altitud: Double,
    val latitud: Double,
    val longitud: Double,
    val temperatura: Double? = null,
    val humedad: Double? = null,
    val vientoVelocidad: Double? = null,
    val vientoDireccion: Double? = null,
    val presion: Double? = null,
    val precipitacion: Double? = null,
    val racha: Double? = null,
    val fechaObservacion: String? = null,
    val isLoadingObservation: Boolean = false,
    val observationError: String? = null
)

data class AemetWarningDomainData(
    val fechaInicio: String?,
    val fechaFin: String?,
    val nivel: String?, // Amarillo, Naranja, Rojo
    val fenomeno: String?,
    val ambitoGeografico: String?, // Zona afectada
    val descripcion: String?
)
