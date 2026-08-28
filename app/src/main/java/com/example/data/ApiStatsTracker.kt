package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

enum class ApiCategory(
    val id: String,
    val displayName: String,
    val endpointHost: String,
    val description: String,
    val emoji: String
) {
    OPEN_METEO_WEATHER(
        "open_meteo_weather",
        "Open-Meteo Clima",
        "api.open-meteo.com/v1/forecast",
        "Previsión horaria 24h, diaria 7d y variables climáticas",
        "☀️"
    ),
    OPEN_METEO_AQI(
        "open_meteo_aqi",
        "Open-Meteo Calidad Aire",
        "air-quality-api.open-meteo.com",
        "Índice ICA, PM2.5, PM10, Ozono y Detección de Calima",
        "💨"
    ),
    OPEN_METEO_MARINE(
        "open_meteo_marine",
        "Open-Meteo Marítimo",
        "marine-api.open-meteo.com",
        "Altura de ola, periodo, dirección y mar de viento",
        "🌊"
    ),
    AEMET_OPENDATA(
        "aemet_opendata",
        "AEMET OpenData Observaciones",
        "opendata.aemet.es/opendata/api/observacion",
        "Observaciones de estaciones meteorológicas en vivo",
        "🏛️"
    ),
    AEMET_WARNINGS(
        "aemet_warnings",
        "AEMET Avisos & Alertas CAP",
        "opendata.aemet.es/opendata/api/avisos_cap",
        "Avisos oficiales por fenómenos meteorológicos adversos",
        "🚨"
    ),
    AEMET_STATIONS(
        "aemet_stations",
        "AEMET Inventario Estaciones",
        "opendata.aemet.es/opendata/api/valores/climatologicos",
        "Catálogo de estaciones oficiales del archipiélago",
        "📡"
    ),
    IHM_TIDES(
        "ihm_tides",
        "IHM Instituto Hidrográfico",
        "ideihm.covam.es",
        "Tablas astronómicas de mareas, pleamar y bajamar",
        "⚓"
    ),
    INFOPLAYAS(
        "infoplayas",
        "Gobierno de Canarias Infoplayas",
        "gobiernodecanarias.org",
        "Banderas de baño y servicio de socorristas en playas",
        "🏖️"
    ),
    GEOCODING(
        "geocoding",
        "Open-Meteo Geocoding",
        "geocoding-api.open-meteo.com",
        "Buscador de localidades y coordenadas geográficas",
        "📍"
    ),
    GOOGLE_DRIVE(
        "google_drive",
        "Google Drive AppData Sync",
        "googleapis.com",
        "Sincronización en la nube privada de favoritos y ajustes",
        "☁️"
    ),
    AEMET_RSS(
        "aemet_rss",
        "AEMET RSS y Documentos Web",
        "www.aemet.es",
        "Feeds RSS, documentos XML y avisos web de AEMET",
        "📰"
    ),
    MAPS_TILES(
        "maps_tiles",
        "Mapas y Teselas Cartográficas",
        "openstreetmap.org",
        "Mapas, teselas cartográficas y visualizadores geográficos",
        "🗺️"
    ),
    OTHER(
        "other",
        "Otras Consultas de Red",
        "external",
        "Peticiones auxiliares y recursos externos",
        "🌐"
    );

    companion object {
        fun fromUrl(url: String): ApiCategory {
            val lower = url.lowercase()
            return when {
                lower.contains("opendata.aemet.es") && lower.contains("avisos_cap") -> AEMET_WARNINGS
                lower.contains("opendata.aemet.es") && lower.contains("inventarioestaciones") -> AEMET_STATIONS
                lower.contains("opendata.aemet.es") -> AEMET_OPENDATA
                lower.contains("www.aemet.es") || lower.contains("aemet.es/documentos_d") -> AEMET_RSS
                lower.contains("air-quality-api.open-meteo.com") -> OPEN_METEO_AQI
                lower.contains("marine-api.open-meteo.com") -> OPEN_METEO_MARINE
                lower.contains("geocoding-api.open-meteo.com") -> GEOCODING
                lower.contains("api.open-meteo.com") -> OPEN_METEO_WEATHER
                lower.contains("ideihm.covam.es") -> IHM_TIDES
                lower.contains("gobiernodecanarias.org") -> INFOPLAYAS
                lower.contains("googleapis.com") -> GOOGLE_DRIVE
                lower.contains("openstreetmap.org") || lower.contains("tile") || lower.contains("maps") -> MAPS_TILES
                else -> OTHER
            }
        }
    }
}

data class SingleApiStats(
    val categoryId: String,
    val totalCalls: Long = 0,
    val successCalls: Long = 0,
    val failedCalls: Long = 0,
    val lastCallTimestamp: Long = 0,
    val lastStatusCode: Int = 0,
    val lastResponseTimeMs: Long = 0,
    val totalResponseTimeMs: Long = 0,
    val lastErrorMessage: String? = null
) {
    val averageResponseTimeMs: Long
        get() = if (totalCalls > 0) totalResponseTimeMs / totalCalls else 0

    val successRate: Int
        get() = if (totalCalls > 0) ((successCalls.toDouble() / totalCalls.toDouble()) * 100).toInt() else 100
}

data class GlobalApiSummary(
    val totalCalls: Long = 0,
    val totalSuccess: Long = 0,
    val totalFailed: Long = 0,
    val successRatePercentage: Int = 100,
    val averageResponseTimeMs: Long = 0,
    val firstRecordedDate: Long = 0,
    val lastCallTimestamp: Long = 0,
    val statsList: List<SingleApiStats> = emptyList()
)

object ApiStatsTracker {
    private const val PREFS_NAME = "clima_canarias_api_stats"
    private const val KEY_FIRST_RECORDED = "first_recorded_time"
    private const val KEY_STATS_PREFIX = "api_stat_"

    private var prefs: SharedPreferences? = null
    private val memoryStats = mutableMapOf<String, SingleApiStats>()

    private val _statsFlow = MutableStateFlow(GlobalApiSummary())
    val statsFlow: StateFlow<GlobalApiSummary> = _statsFlow.asStateFlow()

    @Synchronized
    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
        recalculateSummary()
    }

    @Synchronized
    private fun loadFromPrefs() {
        val p = prefs ?: return
        if (!p.contains(KEY_FIRST_RECORDED)) {
            p.edit().putLong(KEY_FIRST_RECORDED, System.currentTimeMillis()).apply()
        }

        memoryStats.clear()
        for (category in ApiCategory.values()) {
            val jsonStr = p.getString("$KEY_STATS_PREFIX${category.id}", null)
            if (jsonStr != null) {
                try {
                    val obj = JSONObject(jsonStr)
                    val stat = SingleApiStats(
                        categoryId = category.id,
                        totalCalls = obj.optLong("totalCalls", 0),
                        successCalls = obj.optLong("successCalls", 0),
                        failedCalls = obj.optLong("failedCalls", 0),
                        lastCallTimestamp = obj.optLong("lastCallTimestamp", 0),
                        lastStatusCode = obj.optInt("lastStatusCode", 0),
                        lastResponseTimeMs = obj.optLong("lastResponseTimeMs", 0),
                        totalResponseTimeMs = obj.optLong("totalResponseTimeMs", 0),
                        lastErrorMessage = if (obj.has("lastErrorMessage")) obj.optString("lastErrorMessage") else null
                    )
                    memoryStats[category.id] = stat
                } catch (e: Exception) {
                    Log.e("ApiStatsTracker", "Error loading stat for ${category.id}", e)
                    memoryStats[category.id] = SingleApiStats(categoryId = category.id)
                }
            } else {
                memoryStats[category.id] = SingleApiStats(categoryId = category.id)
            }
        }
    }

    @Synchronized
    fun recordCall(
        url: String,
        durationMs: Long,
        statusCode: Int,
        isSuccess: Boolean,
        errorMsg: String? = null
    ) {
        val category = ApiCategory.fromUrl(url)
        val current = memoryStats[category.id] ?: SingleApiStats(categoryId = category.id)
        
        val updated = current.copy(
            totalCalls = current.totalCalls + 1,
            successCalls = if (isSuccess) current.successCalls + 1 else current.successCalls,
            failedCalls = if (!isSuccess) current.failedCalls + 1 else current.failedCalls,
            lastCallTimestamp = System.currentTimeMillis(),
            lastStatusCode = statusCode,
            lastResponseTimeMs = durationMs,
            totalResponseTimeMs = current.totalResponseTimeMs + durationMs,
            lastErrorMessage = errorMsg
        )

        memoryStats[category.id] = updated
        persistStat(updated)
        recalculateSummary()
    }

    private fun persistStat(stat: SingleApiStats) {
        val p = prefs ?: return
        try {
            val obj = JSONObject().apply {
                put("totalCalls", stat.totalCalls)
                put("successCalls", stat.successCalls)
                put("failedCalls", stat.failedCalls)
                put("lastCallTimestamp", stat.lastCallTimestamp)
                put("lastStatusCode", stat.lastStatusCode)
                put("lastResponseTimeMs", stat.lastResponseTimeMs)
                put("totalResponseTimeMs", stat.totalResponseTimeMs)
                if (stat.lastErrorMessage != null) {
                    put("lastErrorMessage", stat.lastErrorMessage)
                }
            }
            p.edit().putString("$KEY_STATS_PREFIX${stat.categoryId}", obj.toString()).apply()
        } catch (e: Exception) {
            Log.e("ApiStatsTracker", "Error persisting stat for ${stat.categoryId}", e)
        }
    }

    @Synchronized
    private fun recalculateSummary() {
        val p = prefs
        val firstDate = p?.getLong(KEY_FIRST_RECORDED, System.currentTimeMillis()) ?: System.currentTimeMillis()
        
        var totalCalls = 0L
        var totalSuccess = 0L
        var totalFailed = 0L
        var totalTime = 0L
        var lastCallTime = 0L

        val list = mutableListOf<SingleApiStats>()

        for (category in ApiCategory.values()) {
            val stat = memoryStats[category.id] ?: SingleApiStats(categoryId = category.id)
            list.add(stat)

            totalCalls += stat.totalCalls
            totalSuccess += stat.successCalls
            totalFailed += stat.failedCalls
            totalTime += stat.totalResponseTimeMs
            if (stat.lastCallTimestamp > lastCallTime) {
                lastCallTime = stat.lastCallTimestamp
            }
        }

        val rate = if (totalCalls > 0) ((totalSuccess.toDouble() / totalCalls.toDouble()) * 100).toInt() else 100
        val avgTime = if (totalCalls > 0) totalTime / totalCalls else 0

        _statsFlow.value = GlobalApiSummary(
            totalCalls = totalCalls,
            totalSuccess = totalSuccess,
            totalFailed = totalFailed,
            successRatePercentage = rate,
            averageResponseTimeMs = avgTime,
            firstRecordedDate = firstDate,
            lastCallTimestamp = lastCallTime,
            statsList = list
        )
    }

    @Synchronized
    fun resetAllStats() {
        val p = prefs ?: return
        p.edit().clear().putLong(KEY_FIRST_RECORDED, System.currentTimeMillis()).apply()
        memoryStats.clear()
        for (cat in ApiCategory.values()) {
            memoryStats[cat.id] = SingleApiStats(categoryId = cat.id)
        }
        recalculateSummary()
    }
}

class ApiStatsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val startTime = System.currentTimeMillis()

        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            val isSuccess = response.isSuccessful
            val code = response.code

            ApiStatsTracker.recordCall(
                url = url,
                durationMs = duration,
                statusCode = code,
                isSuccess = isSuccess,
                errorMsg = if (!isSuccess) "HTTP $code" else null
            )
            response
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            ApiStatsTracker.recordCall(
                url = url,
                durationMs = duration,
                statusCode = 0,
                isSuccess = false,
                errorMsg = e.localizedMessage ?: "Error de conexión"
            )
            throw e
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            ApiStatsTracker.recordCall(
                url = url,
                durationMs = duration,
                statusCode = 0,
                isSuccess = false,
                errorMsg = e.localizedMessage ?: "Excepción inesperada"
            )
            throw e
        }
    }
}
