package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.*
import com.example.db.AppDatabase
import com.example.db.FavoriteCity
import com.example.db.FavoriteBeach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class WeatherRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.favoriteDao()

    val allFavorites: Flow<List<FavoriteCity>> = dao.getAllFavorites()
    val allFavoriteBeaches: Flow<List<FavoriteBeach>> = dao.getAllFavoriteBeaches()

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

    private var cachedStations: List<IhmPuerto>? = null

    suspend fun fetchMarineWeather(lat: Double, lng: Double): Pair<MarineWeatherDto?, List<TideInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = WeatherApiClient.buildMarineWeatherUrl(lat, lng)
                val marineData = WeatherApiClient.api.getMarineWeather(url)
                
                // Fetch Ihm tide data
                var tides = emptyList<TideInfo>()
                try {
                    if (cachedStations == null) {
                        val stationsRes = WeatherApiClient.api.getIhmTideStations()
                        cachedStations = stationsRes.estaciones?.puertos
                    }
                    val stations = cachedStations
                    if (!stations.isNullOrEmpty()) {
                        // Find closest
                        var closestStation: IhmPuerto? = null
                        var minDistance = Float.MAX_VALUE
                        for (station in stations) {
                            val stLat = station.lat.toDoubleOrNull() ?: 0.0
                            val stLon = station.lon.toDoubleOrNull() ?: 0.0
                            val results = FloatArray(1)
                            android.location.Location.distanceBetween(lat, lng, stLat, stLon, results)
                            val dist = results[0]
                            if (dist < minDistance) {
                                minDistance = dist
                                closestStation = station
                            }
                        }
                        closestStation?.id?.let { id ->
                            val tideUrl = "https://ideihm.covam.es/api-ihm/getmarea?request=gettide&id=$id&format=json"
                            val tideRes = WeatherApiClient.api.getIhmTideData(tideUrl)
                            val domainTides = tideRes.mareas?.datos?.marea?.mapNotNull { item ->
                                val h = item.altura.toDoubleOrNull() ?: return@mapNotNull null
                                TideInfo(item.hora, h, item.tipo)
                            } ?: emptyList()
                            tides = domainTides
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Log.e("WeatherRepository", "Error fetching tide data", e)
                }

                Pair(marineData, tides)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("WeatherRepository", "Error fetching marine weather", e)
                Pair(null, emptyList())
            }
        }
    }

    private fun ensureHttps(url: String): String {
        return if (url.startsWith("http://")) {
            url.replace("http://", "https://")
        } else {
            url
        }
    }

    private fun decodeResponseBody(responseBody: okhttp3.ResponseBody): String {
        return responseBody.use { body ->
            val bytes = body.bytes()
            String(bytes, java.nio.charset.Charset.forName("ISO-8859-1"))
        }
    }

    suspend fun fetchAemetWarnings(apiKey: String): List<AemetWarningDomainData> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch directly from AEMET's ATOM feed for Canarias
                val url = "https://www.aemet.es/documentos_d/eltiempo/prediccion/avisos/rss/CAP_AFAC65_ATOM.xml"
                
                val responseBodyRaw = WeatherApiClient.api.getAemetWarnings(url)
                val responseBody = decodeResponseBody(responseBodyRaw).trim()
                
                if (responseBody.isBlank()) {
                    return@withContext emptyList()
                }

                val warnings = mutableListOf<AemetWarningDomainData>()
                val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = true
                val parser = factory.newPullParser()
                parser.setInput(java.io.StringReader(responseBody))
                
                var eventType = parser.eventType
                var currentText = ""
                var inEntry = false
                var title = ""
                var summary = ""
                
                while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                    val tagName = parser.name ?: ""
                    when (eventType) {
                        org.xmlpull.v1.XmlPullParser.START_TAG -> {
                            if (tagName.equals("entry", true)) {
                                inEntry = true
                                title = ""
                                summary = ""
                            }
                        }
                        org.xmlpull.v1.XmlPullParser.TEXT -> {
                            currentText = parser.text ?: ""
                        }
                        org.xmlpull.v1.XmlPullParser.END_TAG -> {
                            if (inEntry) {
                                if (tagName.equals("title", true)) {
                                    title = currentText.trim()
                                } else if (tagName.equals("summary", true)) {
                                    summary = currentText.trim()
                                } else if (tagName.equals("entry", true)) {
                                    inEntry = false
                                    // Process entry
                                    if (!title.contains("Estado completo", ignoreCase = true) && title.isNotEmpty()) {
                                        val parts = title.split(".").map { it.trim() }
                                        val nivel = parts.getOrNull(1)?.replace("Nivel ", "", ignoreCase = true)?.trim()
                                        val fenomeno = parts.getOrNull(2)
                                        val ambito = parts.getOrNull(3)
                                        
                                        warnings.add(
                                            AemetWarningDomainData(
                                                fechaInicio = null, // Can parse from summary if needed
                                                fechaFin = null,
                                                nivel = nivel ?: "amarillo",
                                                fenomeno = fenomeno ?: "Aviso",
                                                ambitoGeografico = ambito ?: "Canarias",
                                                descripcion = summary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
                
                warnings
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("WeatherRepository", "Error fetching AEMET ATOM warnings", e)
                emptyList() // Return empty list to prevent crash
            }
        }
    }

    suspend fun fetchAemetStations(apiKey: String): List<AemetStationDomainData> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://opendata.aemet.es/opendata/api/valores/climatologicos/inventarioestaciones/todasestaciones?api_key=$apiKey"
                val response = WeatherApiClient.api.getAemetApiResponse(url)
                if (response.estado == 401 || response.estado == 429) {
                    throw Exception("AEMET API Error: ${response.descripcion ?: "No autorizado o límite de peticiones."}")
                }
                val datosUrl = response.datos
                if (datosUrl.isNullOrEmpty()) {
                    throw Exception("AEMET Error: ${response.descripcion ?: "Respuesta vacía"}")
                }
                
                // Ensure secure HTTPS URL
                val secureDatosUrl = ensureHttps(datosUrl)
                val responseBody = WeatherApiClient.api.getAemetStations(secureDatosUrl)
                val jsonString = decodeResponseBody(responseBody)
                
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, AemetStationDto::class.java)
                val adapter = WeatherApiClient.moshi.adapter<List<AemetStationDto>>(listType)
                val stationsList = adapter.fromJson(jsonString) ?: emptyList()
                
                stationsList.filter { station ->
                    val prov = station.provincia?.lowercase() ?: ""
                    prov.contains("palmas") || prov.contains("tenerife") || prov.contains("cruz") || prov.contains("canarias")
                }.map { station ->
                    val lat = parseAemetCoordinate(station.latitud) ?: 28.0
                    val lon = parseAemetCoordinate(station.longitud) ?: -15.4
                    
                    AemetStationDomainData(
                        indicativo = station.indicativo ?: "",
                        nombre = station.nombre ?: "Estación Desconocida",
                        provincia = station.provincia ?: "Canarias",
                        altitud = station.altitud ?: 0.0,
                        latitud = lat,
                        longitud = lon
                    )
                }.distinctBy { it.indicativo }.sortedBy { it.nombre }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("WeatherRepository", "Error fetching AEMET stations", e)
                throw e
            }
        }
    }

    suspend fun fetchAemetStationObservation(apiKey: String, indicativo: String): AemetObservationDto? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/$indicativo?api_key=$apiKey"
                val response = WeatherApiClient.api.getAemetApiResponse(url)
                val datosUrl = response.datos
                if (datosUrl.isNullOrEmpty()) {
                    return@withContext null
                }
                
                // Ensure secure HTTPS URL
                val secureDatosUrl = ensureHttps(datosUrl)
                val responseBody = WeatherApiClient.api.getAemetObservations(secureDatosUrl)
                val jsonString = decodeResponseBody(responseBody)
                
                // Parse as a safe List of Maps to bypass Moshi Any restrictions in Android reflection
                val mapType = com.squareup.moshi.Types.newParameterizedType(
                    Map::class.java,
                    String::class.java,
                    Any::class.java
                )
                val listType = com.squareup.moshi.Types.newParameterizedType(
                    List::class.java,
                    mapType
                )
                val adapter = WeatherApiClient.moshi.adapter<List<Map<String, Any?>>>(listType)
                val observations = adapter.fromJson(jsonString) ?: emptyList()
                
                val lastObs = observations.lastOrNull() ?: return@withContext null
                
                AemetObservationDto(
                    fint = lastObs["fint"] as? String,
                    ubi = lastObs["ubi"] as? String,
                    ta = lastObs["ta"],
                    hr = lastObs["hr"],
                    vv = lastObs["vv"],
                    dv = lastObs["dv"],
                    pres = lastObs["pres"],
                    prec = lastObs["prec"]
                )
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("WeatherRepository", "Error fetching AEMET station observation for $indicativo", e)
                null
            }
        }
    }

    private fun parseAemetCoordinate(coordStr: String?): Double? {
        if (coordStr == null) return null
        val cleaned = coordStr.trim()
        if (cleaned.isEmpty()) return null
        val isNegative = cleaned.endsWith("S") || cleaned.endsWith("W") || cleaned.endsWith("O")
        val digitsOnly = cleaned.filter { it.isDigit() }
        if (digitsOnly.length < 6) return null
        
        val degrees = digitsOnly.substring(0, digitsOnly.length - 4).toDoubleOrNull() ?: 0.0
        val minutes = digitsOnly.substring(digitsOnly.length - 4, digitsOnly.length - 2).toDoubleOrNull() ?: 0.0
        val seconds = digitsOnly.substring(digitsOnly.length - 2).toDoubleOrNull() ?: 0.0
        
        val decimal = degrees + (minutes / 60.0) + (seconds / 3600.0)
        return if (isNegative) -decimal else decimal
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
                if (e is kotlinx.coroutines.CancellationException) throw e
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
                        weatherCode = d.weatherCode[i],
                        uvIndexMax = d.uvIndexMax?.getOrNull(i)
                    )
                )
            }
        }

        return WeatherDomainData(
            cityName = cityName,
            latitude = weather.latitude,
            longitude = weather.longitude,
            elevation = weather.elevation,
            temperatureCelsius = current.temperature,
            humidity = current.humidity ?: 62.0,
            windSpeedKmh = current.windSpeed,
            windDirectionDegrees = current.windDirection,
            condition = finalCondition,
            weatherCode = current.weatherCode,
            uvIndex = current.uvIndex,
            airQuality = airQuality,
            hourlyForecast = hourlyItems,
            dailyForecast = dailyItems,
            sunrise = weather.daily?.sunrise?.firstOrNull()?.split("T")?.lastOrNull(),
            sunset = weather.daily?.sunset?.firstOrNull()?.split("T")?.lastOrNull(),
            isSynthetic = false
        )
    }

    suspend fun addFavoriteBeach(id: String, name: String) {
        withContext(Dispatchers.IO) {
            dao.insertFavoriteBeach(FavoriteBeach(id = id, name = name))
        }
    }

    suspend fun removeFavoriteBeach(beach: FavoriteBeach) {
        withContext(Dispatchers.IO) {
            dao.deleteFavoriteBeach(beach)
        }
    }

    suspend fun replaceFavoriteBeaches(beaches: List<FavoriteBeach>) {
        withContext(Dispatchers.IO) {
            dao.deleteAllFavoriteBeaches()
            for (beach in beaches) {
                dao.insertFavoriteBeach(beach)
            }
        }
    }
}
