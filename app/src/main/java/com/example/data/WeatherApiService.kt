package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface WeatherApi {
    @GET
    suspend fun getWeather(
        @Url url: String
    ): OpenMeteoResponse

    @GET
    suspend fun getAirQuality(
        @Url url: String
    ): OpenMeteoAqiResponse

    @GET
    suspend fun getMarineWeather(
        @Url url: String
    ): MarineWeatherDto

    @GET
    suspend fun getIhmTideStations(
        @Url url: String = "http://ideihm.covam.es/api-ihm/getmarea?request=getlist&format=json"
    ): IhmTideStationListResponse

    @GET
    suspend fun getIhmTideData(
        @Url url: String
    ): IhmTideResponse
    @GET
    suspend fun getInfoPlayasBeaches(
        @Url url: String = "https://www3.gobiernodecanarias.org/aplicaciones/infoplayas/socorrismo/api/beach"
    ): InfoPlayasBeachResponse

    @GET
    suspend fun getInfoPlayasFlags(
        @Url url: String = "https://www3.gobiernodecanarias.org/aplicaciones/infoplayas/socorrismo/api/flags"
    ): InfoPlayasFlagsResponse

    @GET
    suspend fun getAemetApiResponse(
        @Url url: String
    ): AemetApiResponse

    @GET
    suspend fun getAemetStations(
        @Url url: String
    ): okhttp3.ResponseBody

    @GET
    suspend fun getAemetObservations(
        @Url url: String
    ): okhttp3.ResponseBody

    @GET
    suspend fun getAemetWarnings(
        @Url url: String
    ): okhttp3.ResponseBody
    @GET
    suspend fun searchLocation(@Url url: String): GeocodingResponse
}

data class AemetApiResponse(
    val descripcion: String?,
    val estado: Int?,
    val datos: String?,
    val metadatos: String?
)

data class AemetStationDto(
    val latitud: String?,
    val provincia: String?,
    val altitud: Double?,
    val indicativo: String?,
    val nombre: String?,
    val longitud: String?,
    val indsinop: String?
)

data class AemetObservationDto(
    val fint: String?,
    val ubi: String?,
    val ta: Any?,
    val hr: Any?,
    val vv: Any?,
    val dv: Any?,
    val pres: Any?,
    val prec: Any?,
    val vmax: Any?
)

data class InfoPlayasBeachResponse(
    val data: List<InfoPlayasBeach>
)

data class LifeguardInfo(
    val id: Int?,
    @Json(name = "init_date") val initDate: String?,
    @Json(name = "end_date") val endDate: String?,
    val company: String?,
    val period: String?,
    @Json(name = "beach_id") val beachId: Int?,
    @Json(name = "init_hour") val initHour: String?,
    @Json(name = "end_hour") val endHour: String?
)

data class InfoPlayasBeach(
    val id: Int,
    val name: String?,
    val dgse: Any?,
    val lifeguard: List<LifeguardInfo>?
)

data class InfoPlayasFlagsResponse(
    val data: List<InfoPlayasFlag>
)

data class InfoPlayasFlag(
    val id: Int,
    @Json(name = "beach_location_id") val beachLocationId: Int,
    val flag: Int?,
    val reason: String?,
    @Json(name = "water_temp") val waterTemp: Double?,
    @Json(name = "wind_speed") val windSpeed: String?,
    @Json(name = "wind_orientation") val windOrientation: String?,
    val uvdb: Any?,
    val temperature: String?
)

object WeatherApiClient {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiStatsInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/") // Fallback default
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApi::class.java)
    }

    // Builder helpers for request URLs
    fun buildWeatherUrl(lat: Double, lng: Double): String {
        return "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat" +
                "&longitude=$lng" +
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,weather_code,uv_index" +
                "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,wind_speed_10m,wind_direction_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset,uv_index_max" +
                "&timezone=Atlantic/Canary" +
                "&forecast_days=7"
    }

    fun buildAirQualityUrl(lat: Double, lng: Double): String {
        return "https://air-quality-api.open-meteo.com/v1/air-quality?" +
                "latitude=$lat" +
                "&longitude=$lng" +
                "&current=pm2_5,pm10,nitrogen_dioxide,ozone,sulphur_dioxide" +
                "&timezone=Atlantic/Canary"
    }

    fun buildMarineWeatherUrl(lat: Double, lng: Double): String {
        return "https://marine-api.open-meteo.com/v1/marine?" +
                "latitude=$lat" +
                "&longitude=$lng" +
                "&hourly=wave_height,wave_direction,wave_period,wind_wave_height,wind_wave_direction,wind_wave_period,swell_wave_height,swell_wave_direction,swell_wave_period" +
                "&timezone=Atlantic/Canary"
    }
}


data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val admin1: String? = null
)
