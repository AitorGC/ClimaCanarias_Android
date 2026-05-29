package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Beach(
    val name: String,
    val island: String,
    val municipality: String,
    val province: String,
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

@JsonClass(generateAdapter = true)
data class IhmTideStationListResponse(
    val estaciones: IhmEstaciones?
)

@JsonClass(generateAdapter = true)
data class IhmEstaciones(
    val puertos: List<IhmPuerto>?
)

@JsonClass(generateAdapter = true)
data class IhmPuerto(
    val id: String,
    val code: String,
    val puerto: String,
    val lat: String,
    val lon: String
)

@JsonClass(generateAdapter = true)
data class IhmTideResponse(
    val mareas: IhmMareas?
)

@JsonClass(generateAdapter = true)
data class IhmMareas(
    val datos: IhmDatos?
)

@JsonClass(generateAdapter = true)
data class IhmDatos(
    val marea: List<IhmMareaDataItem>?
)

@JsonClass(generateAdapter = true)
data class IhmMareaDataItem(
    val hora: String,
    val altura: String,
    val tipo: String
)

data class TideInfo(
    val time: String,
    val height: Double,
    val type: String // "pleamar" or "bajamar"
)
