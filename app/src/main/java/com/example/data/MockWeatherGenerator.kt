package com.example.data

import kotlin.math.sin

object MockWeatherGenerator {

    fun generateFallbackData(cityName: String, lat: Double, lng: Double): WeatherDomainData {
        // Base values per city profile based on coordinates or names
        val isLanzarote = lat > 28.8 || cityName.contains("Arrecife", ignoreCase = true)
        val isLaLaguna = cityName.contains("Laguna", ignoreCase = true) || (lat in 28.47..28.50 && lng in -16.33..-16.30)
        val isLasPalmas = cityName.contains("Palmas", ignoreCase = true)
        
        val baseTemp = when {
            isLanzarote -> 24.5
            isLaLaguna -> 17.5
            isLasPalmas -> 22.0
            else -> 21.0
        }
        
        val baseHumidity = when {
            isLaLaguna -> 85.0
            isLanzarote -> 55.0
            else -> 70.0
        }

        // Generate synthetic hourly list representing temperature cycles (high in afternoon)
        val hourlyItems = ArrayList<HourlyForecastItem>()
        val currentHour = 12
        for (i in 0 until 24) {
            val hourValue = (currentHour + i) % 24
            val timeStr = String.format("%02d:00", hourValue)
            
            // Temperature peak at 15:00, coldest at 05:00
            val tempOffset = 4.0 * sin((hourValue - 9) * Math.PI / 12.0)
            val hourlyTemp = baseTemp + tempOffset
            
            // Inverse relationship: hum is low when temp is high
            val humOffset = -15 * sin((hourValue - 9) * Math.PI / 12.0)
            val hourlyHum = (baseHumidity + humOffset).coerceIn(10.0, 98.0)
            
            // Precipitation probability
            val hourlyPrecip = when {
                isLaLaguna -> (30 + 15 * sin(hourValue * Math.PI / 6.0)).toInt().coerceIn(0, 100)
                isLanzarote -> (2 + sin(hourValue * Math.PI / 12.0) * 5).toInt().coerceIn(0, 100)
                else -> (10 + sin(hourValue * Math.PI / 12.0) * 10).toInt().coerceIn(0, 100)
            }
            
            hourlyItems.add(
                HourlyForecastItem(
                    timeString = timeStr,
                    temperature = String.format("%.1f", hourlyTemp).toDouble(),
                    humidity = String.format("%.1f", hourlyHum).toDouble(),
                    precipitationProbability = hourlyPrecip
                )
            )
        }

        // Setup wind for Calima conditions
        // In Lanzarote, east/southeast wind often triggers Calima! (120 degrees is East-Southeast)
        val (windDir, windSpeed) = if (isLanzarote) {
            Pair(115.0, 32.5) // ESE wind
        } else {
            Pair(30.0, 22.0) // NNE Trade wind (Alisios)
        }

        val condition = when {
            isLanzarote -> WeatherCondition.CALIMA
            isLaLaguna -> WeatherCondition.RAINY
            isLasPalmas -> WeatherCondition.CLOUDY
            else -> WeatherCondition.SUNNY
        }

        val weatherCode = when (condition) {
            WeatherCondition.SUNNY -> 0
            WeatherCondition.CLOUDY -> 3
            WeatherCondition.CALIMA -> 5
            WeatherCondition.RAINY -> 61
            WeatherCondition.SNOWY -> 71
            WeatherCondition.STORM -> 95
        }

        // Setup AQI
        val pm10 = if (condition == WeatherCondition.CALIMA) 145.0 else 18.2
        val pm25 = if (condition == WeatherCondition.CALIMA) 62.0 else 8.5
        val no2 = 14.0
        val o3 = 45.0
        val co = 0.35

        // European AQI Calculation (simple index 1-5, where 1=good, 5=very poor)
        val europeanAqi = when {
            pm10 > 100 -> 4 // Poor
            pm10 > 50 -> 3 // Fair
            pm10 > 20 -> 2 // Good
            else -> 1 // Very Good
        }

        val americanAqi = when {
            pm10 > 150 -> 151 // Unhealthy
            pm10 > 50 -> 65 // Moderate
            else -> 32 // Good
        }

        val calimaSeverity = if (condition == WeatherCondition.CALIMA) CalimaSeverity.SEVERE else CalimaSeverity.NONE
        val calimaAlertMessage = if (condition == WeatherCondition.CALIMA) {
            "AVISO AEMET: Afluencia de polvo sahariano en suspensión. Se aconseja mantener ventanas cerradas y evitar actividades físicas prolongadas al aire libre."
        } else null

        val airQuality = AirQualityData(
            pm25 = pm25,
            pm10 = pm10,
            no2 = no2,
            o3 = o3,
            co = co,
            europeanAqi = europeanAqi,
            americanAqi = americanAqi,
            calimaSeverity = calimaSeverity,
            calimaAlertMessage = calimaAlertMessage
        )

        // Generate synthetic daily forecast for 7 days
        val dailyItems = ArrayList<DailyForecastItem>()
        for (i in 0 until 7) {
            val dateLabel = if (i == 0) "Hoy" else if (i == 1) "Mañana" else {
                val dayOffset = (3 + i) % 7 // Wednesday May 27, 2026 is Wed = 3
                when (dayOffset) {
                    0 -> "Domingo"
                    1 -> "Lunes"
                    2 -> "Martes"
                    3 -> "Miércoles"
                    4 -> "Jueves"
                    5 -> "Viernes"
                    6 -> "Sábado"
                    else -> "Día $i"
                }
            }
            
            val maxOffset = 2.0 * sin(i * Math.PI / 3.0) + (if (condition == WeatherCondition.CALIMA) 4.0 else 0.0)
            val minOffset = 1.0 * sin((i + 1) * Math.PI / 3.0)
            val dailyMax = baseTemp + 4.0 + maxOffset
            val dailyMin = baseTemp - 3.0 + minOffset
            val rainProb = when {
                isLaLaguna -> (30 + 15 * sin(i * Math.PI / 3.0)).toInt().coerceIn(0, 100)
                isLanzarote -> (2 + sin(i * Math.PI / 4.0) * 8).toInt().coerceIn(0, 100)
                else -> (10 + sin(i * Math.PI / 4.0) * 15).toInt().coerceIn(0, 100)
            }
            
            val dailyCondition = when {
                condition == WeatherCondition.CALIMA && i < 3 -> WeatherCondition.CALIMA
                rainProb > 40 -> WeatherCondition.RAINY
                i % 3 == 0 -> WeatherCondition.CLOUDY
                else -> WeatherCondition.SUNNY
            }
            
            val dailyCode = when (dailyCondition) {
                WeatherCondition.SUNNY -> 0
                WeatherCondition.CLOUDY -> 3
                WeatherCondition.CALIMA -> 5
                WeatherCondition.RAINY -> 61
                WeatherCondition.SNOWY -> 71
                WeatherCondition.STORM -> 95
            }

            dailyItems.add(
                DailyForecastItem(
                    dateString = dateLabel,
                    maxTemp = String.format("%.1f", dailyMax).toDouble(),
                    minTemp = String.format("%.1f", dailyMin).toDouble(),
                    precipitationProbability = rainProb,
                    condition = dailyCondition,
                    weatherCode = dailyCode,
                    uvIndexMax = 8.0 + (i % 3)
                )
            )
        }

        return WeatherDomainData(
            cityName = cityName,
            latitude = lat,
            longitude = lng,
            temperatureCelsius = baseTemp,
            humidity = baseHumidity,
            windSpeedKmh = windSpeed,
            windDirectionDegrees = windDir,
            condition = condition,
            weatherCode = weatherCode,
            uvIndex = 9.0,
            airQuality = airQuality,
            hourlyForecast = hourlyItems,
            dailyForecast = dailyItems,
            sunrise = "07:07",
            sunset = "20:50",
            isSynthetic = true
        )
    }
}
