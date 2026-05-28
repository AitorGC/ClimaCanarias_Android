package com.example.data

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
}

object WeatherApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
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
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,weather_code" +
                "&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,wind_speed_10m,wind_direction_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&timezone=Atlantic/Canary" +
                "&forecast_days=7"
    }

    fun buildAirQualityUrl(lat: Double, lng: Double): String {
        return "https://air-quality-api.open-meteo.com/v1/air-quality?" +
                "latitude=$lat" +
                "&longitude=$lng" +
                "&current=pm2_5,pm10,nitrogen_dioxide,ozone,carbon_monoxide" +
                "&timezone=Atlantic/Canary"
    }
}
