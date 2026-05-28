package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Beach(
    val name: String,
    val island: String,
    val municipality: String,
    val lat: Double,
    val lng: Double,
    val composition: String
)

@JsonClass(generateAdapter = true)
data class MarineWeatherDto(
    val hourly: MarineHourlyDto? = null
)

@JsonClass(generateAdapter = true)
data class MarineHourlyDto(
    val time: List<String>?,
    @Json(name = "wave_height") val waveHeight: List<Double?>?,
    @Json(name = "wave_direction") val waveDirection: List<Double?>?,
    @Json(name = "wave_period") val wavePeriod: List<Double?>?,
    @Json(name = "wind_wave_height") val windWaveHeight: List<Double?>?,
    @Json(name = "wind_wave_direction") val windWaveDirection: List<Double?>?,
    @Json(name = "wind_wave_period") val windWavePeriod: List<Double?>?
)
