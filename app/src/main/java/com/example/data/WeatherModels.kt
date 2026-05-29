package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
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
    val sunset: List<String>?
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Double?,
    @Json(name = "wind_speed_10m") val windSpeed: Double,
    @Json(name = "wind_direction_10m") val windDirection: Double,
    @Json(name = "weather_code") val weatherCode: Int
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
    @Json(name = "carbon_monoxide") val co: Double?
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
    val co: Double,
    val europeanAqi: Int, // Calculated index
    val americanAqi: Int, // Calculated index
    val calimaSeverity: CalimaSeverity,
    val calimaAlertMessage: String?
)

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
    val weatherCode: Int
)

data class WeatherDomainData(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureCelsius: Double,
    val humidity: Double,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Double,
    val condition: WeatherCondition,
    val weatherCode: Int,
    val airQuality: AirQualityData?,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem> = emptyList(),
    val sunrise: String? = null,
    val sunset: String? = null,
    val isSynthetic: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
