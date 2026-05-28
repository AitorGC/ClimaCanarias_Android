package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.*
import com.example.db.AppDatabase
import com.example.db.FavoriteCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class WeatherRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.favoriteDao()

    val allFavorites: Flow<List<FavoriteCity>> = dao.getAllFavorites()

    // Flag for active connection status
    var isConnected: Boolean = true

    suspend fun initializePredefinedCitiesIfEmpty() {
        withContext(Dispatchers.IO) {
            try {
                if (dao.getCount() == 0) {
                    val initialList = listOf(
                        FavoriteCity(name = "Las Palmas de GC", latitude = 28.1235, longitude = -15.4362, isPredefined = true),
                        FavoriteCity(name = "Santa Cruz de Tenerife", latitude = 28.4636, longitude = -16.2518, isPredefined = true),
                        FavoriteCity(name = "La Laguna", latitude = 28.4874, longitude = -16.3159, isPredefined = true),
                        FavoriteCity(name = "Arrecife (Lanzarote)", latitude = 28.9630, longitude = -13.5501, isPredefined = true)
                    )
                    for (city in initialList) {
                        dao.insertFavorite(city)
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error initializing Room favorites", e)
            }
        }
    }

    suspend fun addFavorite(name: String, latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            dao.insertFavorite(
                FavoriteCity(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    isSynced = false
                )
            )
        }
    }

    suspend fun removeFavorite(city: FavoriteCity) {
        withContext(Dispatchers.IO) {
            dao.deleteFavorite(city)
        }
    }

    suspend fun restorePredefinedCities() {
        withContext(Dispatchers.IO) {
            try {
                val current = dao.getFavoritesListSync()
                val initialList = listOf(
                    FavoriteCity(name = "Las Palmas de GC", latitude = 28.1235, longitude = -15.4362, isPredefined = true),
                    FavoriteCity(name = "Santa Cruz de Tenerife", latitude = 28.4636, longitude = -16.2518, isPredefined = true),
                    FavoriteCity(name = "La Laguna", latitude = 28.4874, longitude = -16.3159, isPredefined = true),
                    FavoriteCity(name = "Arrecife (Lanzarote)", latitude = 28.9630, longitude = -13.5501, isPredefined = true)
                )
                for (city in initialList) {
                    if (current.none { it.name == city.name }) {
                        dao.insertFavorite(city)
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error restoring predefined favorites", e)
            }
        }
    }

    suspend fun removeFavoriteById(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteFavoriteById(id)
        }
    }

    suspend fun getCount(): Int {
        return withContext(Dispatchers.IO) {
            dao.getCount()
        }
    }

    suspend fun replaceFavorites(list: List<FavoriteCity>) {
        withContext(Dispatchers.IO) {
            try {
                dao.deleteAllFavorites()
                for (city in list) {
                    dao.insertFavorite(city.copy(id = 0)) // Give it auto-generate ID
                }
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Error replacing favorites", e)
            }
        }
    }

    // Main weather resolver
    suspend fun fetchWeather(cityName: String, lat: Double, lng: Double): WeatherDomainData {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch Core data
                val weatherUrl = WeatherApiClient.buildWeatherUrl(lat, lng)
                val response = WeatherApiClient.api.getWeather(weatherUrl)
                
                // Fetch Air Quality data
                val aqiUrl = WeatherApiClient.buildAirQualityUrl(lat, lng)
                val aqiResponse = WeatherApiClient.api.getAirQuality(aqiUrl)
                
                // Convert response models to unified Domain Models
                convertToDomain(cityName, response, aqiResponse)
            } catch (e: Exception) {
                Log.e("WeatherRepository", "Network fetch failed or rate-limited. Serving fallback. Msg: ${e.message}")
                // Return synthetic domain data
                MockWeatherGenerator.generateFallbackData(cityName, lat, lng)
            }
        }
    }

    private fun convertToDomain(
        cityName: String,
        weather: OpenMeteoResponse,
        aqi: OpenMeteoAqiResponse
    ): WeatherDomainData {
        val current = weather.current ?: throw Exception("Invalid current weather data")
        
        // Compile hourly forecast
        val hourlyItems = ArrayList<HourlyForecastItem>()
        weather.hourly?.let { h ->
            // Max 24 hours
            val totalHours = h.time.size.coerceAtMost(24)
            for (i in 0 until totalHours) {
                val timeStr = h.time[i].substringAfter("T") // Extract just the "HH:MM" block
                hourlyItems.add(
                    HourlyForecastItem(
                        timeString = timeStr,
                        temperature = h.temperature[i],
                        humidity = h.humidity[i],
                        precipitationProbability = h.precipitationProbability[i]
                    )
                )
            }
        }

        // Determine Condition Type from weather_code
        // WMO code map
        val condition = when (current.weatherCode) {
            0 -> WeatherCondition.SUNNY
            1, 2, 3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.CLOUDY
            51, 53, 55, 56, 57 -> WeatherCondition.RAINY
            61, 63, 65, 66, 67 -> WeatherCondition.RAINY
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOWY
            80, 81, 82 -> WeatherCondition.RAINY
            95, 96, 99 -> WeatherCondition.STORM
            else -> WeatherCondition.SUNNY
        }

        // Parse and calculate AQI
        val curAqi = aqi.current
        val pm25 = curAqi?.pm25 ?: 8.0
        val pm10 = curAqi?.pm10 ?: 15.0
        val no2 = curAqi?.no2 ?: 12.0
        val o3 = curAqi?.o3 ?: 40.0
        val co = curAqi?.co ?: 0.3

        // Calima assessment (Eastern winds + suspended dust)
        // Winds from E/SE represent angles roughly from 65° to 155° in Canary Islands vertientes
        val windDir = current.windDirection
        val windSpeed = current.windSpeed
        val isEastWind = windDir in 65.0..155.0
        
        val calimaSeverity = when {
            isEastWind && pm10 > 100.0 && windSpeed > 15.0 -> CalimaSeverity.SEVERE
            isEastWind && pm10 > 50.0 -> CalimaSeverity.MODERATE
            pm10 > 60.0 -> CalimaSeverity.LOW
            else -> CalimaSeverity.NONE
        }

        // Determine final localized condition (override with Calima if severe)
        val finalCondition = if (calimaSeverity == CalimaSeverity.SEVERE || calimaSeverity == CalimaSeverity.MODERATE) {
            WeatherCondition.CALIMA
        } else {
            condition
        }

        val calimaAlertMessage = when (calimaSeverity) {
            CalimaSeverity.SEVERE -> "AVISO METEOROLÓGICO: Calima Severa detectada. Altas concentraciones de polvo sahariano. Evite salir al exterior y use mascarilla."
            CalimaSeverity.MODERATE -> "ALERTA SENSING: Presencia de Calima moderada producida por viento de vertiente este/sureste. Se aconseja precaución."
            else -> null
        }

        // Standard indexes calculations
        val europeanAqi = when {
            pm10 > 150 -> 5
            pm10 > 100 -> 4
            pm10 > 50 -> 3
            pm10 > 25 -> 2
            else -> 1
        }

        val americanAqi = when {
            pm10 > 150 -> 151
            pm10 > 50 -> 72
            else -> 42
        }

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

        // Compile daily 7-day forecast
        val dailyItems = ArrayList<DailyForecastItem>()
        weather.daily?.let { d ->
            val totalDays = d.time.size
            for (i in 0 until totalDays) {
                val rawDate = d.time[i] // e.g. "2026-05-27"
                val dateLabel = try {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val parsedDate = sdf.parse(rawDate)
                    if (parsedDate != null) {
                        val todaySdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val todayStr = todaySdf.format(java.util.Date())
                        
                        val calendar = java.util.Calendar.getInstance()
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                        val tomorrowStr = todaySdf.format(calendar.time)

                        when (rawDate) {
                            todayStr -> "Hoy"
                            tomorrowStr -> "Mañana"
                            else -> {
                                val outSdf = java.text.SimpleDateFormat("EEEE", java.util.Locale("es", "ES"))
                                val formatted = outSdf.format(parsedDate)
                                formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                            }
                        }
                    } else {
                        rawDate
                    }
                } catch (e: Exception) {
                    rawDate
                }

                val dailyCond = when (d.weatherCode[i]) {
                    0 -> WeatherCondition.SUNNY
                    1, 2, 3 -> WeatherCondition.CLOUDY
                    45, 48 -> WeatherCondition.CLOUDY
                    51, 53, 55, 56, 57 -> WeatherCondition.RAINY
                    61, 63, 65, 66, 67 -> WeatherCondition.RAINY
                    71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOWY
                    80, 81, 82 -> WeatherCondition.RAINY
                    95, 96, 99 -> WeatherCondition.STORM
                    else -> WeatherCondition.SUNNY
                }

                dailyItems.add(
                    DailyForecastItem(
                        dateString = dateLabel,
                        maxTemp = d.temperatureMax[i],
                        minTemp = d.temperatureMin[i],
                        precipitationProbability = d.precipitationProbabilityMax?.getOrNull(i) ?: 0,
                        condition = dailyCond,
                        weatherCode = d.weatherCode[i]
                    )
                )
            }
        }

        return WeatherDomainData(
            cityName = cityName,
            latitude = weather.latitude,
            longitude = weather.longitude,
            temperatureCelsius = current.temperature,
            humidity = current.humidity ?: 62.0,
            windSpeedKmh = current.windSpeed,
            windDirectionDegrees = current.windDirection,
            condition = finalCondition,
            weatherCode = current.weatherCode,
            airQuality = airQuality,
            hourlyForecast = hourlyItems,
            dailyForecast = dailyItems,
            isSynthetic = false
        )
    }
}
