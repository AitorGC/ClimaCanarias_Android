package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val cityKey: String, // e.g. "las_palmas_de_gc" or "lat_lng"
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val temperatureCelsius: Double,
    val humidity: Double,
    val windSpeedKmh: Double,
    val windDirectionDegrees: Double,
    val conditionName: String,
    val weatherCode: Int,
    val uvIndex: Double?,
    val sunrise: String?,
    val sunset: String?,
    val airQualityJson: String?, // JSON serialized AirQualityData
    val hourlyForecastJson: String?, // JSON serialized List<HourlyForecastItem>
    val dailyForecastJson: String?, // JSON serialized List<DailyForecastItem>
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
