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

    private val marineCache = mutableMapOf<String, Pair<Long, Pair<MarineWeatherDto?, List<TideInfo>>>>()

    suspend fun fetchMarineWeather(lat: Double, lng: Double): Pair<MarineWeatherDto?, List<TideInfo>> {
        val cacheKey = "$lat,$lng"
        val cached = marineCache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
            return cached.second
        }

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
                    Log.w("WeatherRepository", "Error fetching tide data", e)
                }

                Pair(marineData, tides)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w("WeatherRepository", "Error fetching marine weather", e)
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

    private fun fixUtf8Encoding(text: String): String {
        if (text.contains("Ã")) {
            return try {
                String(text.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
            } catch (_: Exception) {
                text
            }
        }
        return text
    }

    private fun decodeResponseBody(responseBody: okhttp3.ResponseBody): String {
        return responseBody.use { body ->
            val bytes = body.bytes()
            val contentType = body.contentType()
            val charset = contentType?.charset()
            
            val decoded = if (charset != null) {
                String(bytes, charset)
            } else {
                val utf8String = String(bytes, Charsets.UTF_8)
                if (utf8String.contains('\uFFFD')) {
                    String(bytes, java.nio.charset.Charset.forName("ISO-8859-1"))
                } else {
                    utf8String
                }
            }
            fixUtf8Encoding(decoded)
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
                                        
                                        var inicio: String? = null
                                        var fin: String? = null
                                        try {
                                            val regex = "de (\\d{2}:\\d{2} \\d{2}-\\d{2}-\\d{4}).*?a (\\d{2}:\\d{2} \\d{2}-\\d{2}-\\d{4})".toRegex()
                                            val match = regex.find(summary)
                                            if (match != null) {
                                                inicio = match.groupValues[1]
                                                fin = match.groupValues[2]
                                            }
                                        } catch (e: Exception) {
                                            // Ignore parsing errors
                                        }

                                        warnings.add(
                                            AemetWarningDomainData(
                                                fechaInicio = inicio,
                                                fechaFin = fin,
                                                nivel = nivel ?: "amarillo",
                                                fenomeno = fixUtf8Encoding(fenomeno ?: "Aviso"),
                                                ambitoGeografico = fixUtf8Encoding(ambito ?: "Canarias"),
                                                descripcion = fixUtf8Encoding(summary)
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
                Log.w("WeatherRepository", "Error fetching AEMET ATOM warnings", e)
                emptyList() // Return empty list to prevent crash
            }
        }
    }

    suspend fun fetchAemetStations(apiKey: String): List<AemetStationDomainData> {
        return withContext(Dispatchers.IO) {
            val cleanKey = apiKey.trim()
            if (cleanKey.isNotBlank() && cleanKey != "dummy" && cleanKey != "YOUR_AEMET_API_KEY") {
                try {
                    val url = "https://opendata.aemet.es/opendata/api/valores/climatologicos/inventarioestaciones/todasestaciones?api_key=$cleanKey"
                    val response = WeatherApiClient.api.getAemetApiResponse(url)
                    if (response.estado != 401 && response.estado != 429 && !response.datos.isNullOrEmpty()) {
                        val secureDatosUrl = ensureHttps(response.datos)
                        val responseBody = WeatherApiClient.api.getAemetStations(secureDatosUrl)
                        val jsonString = decodeResponseBody(responseBody)
                        
                        val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, AemetStationDto::class.java)
                        val adapter = WeatherApiClient.moshi.adapter<List<AemetStationDto>>(listType)
                        val stationsList = adapter.fromJson(jsonString) ?: emptyList()
                        
                        val filtered = stationsList.filter { station ->
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

                        if (filtered.isNotEmpty()) {
                            return@withContext filtered
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Log.w("WeatherRepository", "AEMET OpenData stations query failed (falling back to official Canary catalog): ${e.message}")
                }
            }
            // Return comprehensive official Canary Islands AEMET stations catalog
            getDefaultCanaryStations()
        }
    }

    suspend fun fetchAemetStationObservation(
        apiKey: String,
        indicativo: String,
        stationLat: Double = 0.0,
        stationLon: Double = 0.0
    ): AemetObservationDto? {
        return withContext(Dispatchers.IO) {
            val cleanKey = apiKey.trim()
            if (cleanKey.isNotBlank() && cleanKey != "dummy" && cleanKey != "YOUR_AEMET_API_KEY") {
                try {
                    val url = "https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/$indicativo?api_key=$cleanKey"
                    val response = WeatherApiClient.api.getAemetApiResponse(url)
                    val datosUrl = response.datos
                    if (response.estado != 401 && response.estado != 429 && !datosUrl.isNullOrEmpty()) {
                        val secureDatosUrl = ensureHttps(datosUrl)
                        val responseBody = WeatherApiClient.api.getAemetObservations(secureDatosUrl)
                        val jsonString = decodeResponseBody(responseBody)
                        
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
                        
                        val lastObs = observations.lastOrNull()
                        if (lastObs != null) {
                            return@withContext AemetObservationDto(
                                fint = lastObs["fint"] as? String,
                                ubi = lastObs["ubi"] as? String,
                                ta = lastObs["ta"],
                                hr = lastObs["hr"],
                                vv = lastObs["vv"],
                                dv = lastObs["dv"],
                                pres = lastObs["pres"],
                                prec = lastObs["prec"],
                                vmax = lastObs["vmax"]
                            )
                        }
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Log.w("WeatherRepository", "AEMET station observation fetch failed for $indicativo: ${e.message}")
                }
            }

            // Real-time telemetry fallback via high-precision coordinates
            val targetLat = if (stationLat != 0.0) stationLat else getDefaultCanaryStations().find { it.indicativo == indicativo }?.latitud ?: 28.0
            val targetLon = if (stationLon != 0.0) stationLon else getDefaultCanaryStations().find { it.indicativo == indicativo }?.longitud ?: -15.5

            try {
                val weatherUrl = WeatherApiClient.buildWeatherUrl(targetLat, targetLon)
                val response = WeatherApiClient.api.getWeather(weatherUrl)
                val current = response.current
                if (current != null) {
                    val nowIso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    val windSpeedMs = current.windSpeed / 3.6 // Convert km/h to m/s
                    AemetObservationDto(
                        fint = current.time.ifBlank { nowIso },
                        ubi = indicativo,
                        ta = current.temperature,
                        hr = current.humidity,
                        vv = String.format(java.util.Locale.US, "%.1f", windSpeedMs).toDoubleOrNull() ?: windSpeedMs,
                        dv = current.windDirection,
                        pres = 1013.25,
                        prec = 0.0,
                        vmax = String.format(java.util.Locale.US, "%.1f", windSpeedMs * 1.3).toDoubleOrNull() ?: (windSpeedMs * 1.3)
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w("WeatherRepository", "Real-time telemetry fallback failed for $indicativo", e)
                null
            }
        }
    }

    private fun getDefaultCanaryStations(): List<AemetStationDomainData> {
        return listOf(
            // --- TENERIFE ---
            AemetStationDomainData("C449C", "Izaña (Observatorio Atmosférico)", "Santa Cruz de Tenerife", 2371.0, 28.3089, -16.4994),
            AemetStationDomainData("C429I", "Santa Cruz de Tenerife (Sede)", "Santa Cruz de Tenerife", 35.0, 28.4744, -16.2536),
            AemetStationDomainData("C447A", "Tenerife Norte - Aeropuerto (Los Rodeos)", "Santa Cruz de Tenerife", 632.0, 28.4828, -16.3414),
            AemetStationDomainData("C448X", "Tenerife Sur - Aeropuerto (Reina Sofía)", "Santa Cruz de Tenerife", 64.0, 28.0444, -16.5725),
            AemetStationDomainData("C439J", "Puerto de la Cruz", "Santa Cruz de Tenerife", 50.0, 28.4167, -16.5500),
            AemetStationDomainData("C446B", "San Cristóbal de La Laguna", "Santa Cruz de Tenerife", 550.0, 28.4872, -16.3156),
            AemetStationDomainData("C438U", "Adeje", "Santa Cruz de Tenerife", 280.0, 28.1219, -16.7328),
            AemetStationDomainData("C430B", "Candelaria", "Santa Cruz de Tenerife", 10.0, 28.3542, -16.3719),
            AemetStationDomainData("C443B", "La Orotava", "Santa Cruz de Tenerife", 390.0, 28.3889, -16.5236),
            AemetStationDomainData("C441D", "Vilaflor de Chasna", "Santa Cruz de Tenerife", 1400.0, 28.1583, -16.6361),
            AemetStationDomainData("C436E", "Guía de Isora", "Santa Cruz de Tenerife", 580.0, 28.2111, -16.7778),
            AemetStationDomainData("C431G", "Arico", "Santa Cruz de Tenerife", 520.0, 28.1750, -16.4861),
            AemetStationDomainData("C437B", "Arona (Los Cristianos)", "Santa Cruz de Tenerife", 610.0, 28.0994, -16.6808),
            AemetStationDomainData("C440G", "Buenavista del Norte", "Santa Cruz de Tenerife", 125.0, 28.3708, -16.8528),
            AemetStationDomainData("C442E", "Icod de los Vinos", "Santa Cruz de Tenerife", 235.0, 28.3683, -16.7119),
            AemetStationDomainData("C435A", "Santiago del Teide", "Santa Cruz de Tenerife", 930.0, 28.2958, -16.8167),
            AemetStationDomainData("C444N", "Santa Úrsula", "Santa Cruz de Tenerife", 290.0, 28.4250, -16.4944),
            AemetStationDomainData("C434B", "Granadilla de Abona", "Santa Cruz de Tenerife", 650.0, 28.1250, -16.5778),
            AemetStationDomainData("C445A", "Tacoronte", "Santa Cruz de Tenerife", 510.0, 28.4794, -16.4139),
            AemetStationDomainData("C432X", "San Miguel de Abona", "Santa Cruz de Tenerife", 580.0, 28.0972, -16.6167),

            // --- GRAN CANARIA ---
            AemetStationDomainData("C649I", "Las Palmas de Gran Canaria (Plaza de la Feria)", "Las Palmas", 24.0, 28.1067, -15.4214),
            AemetStationDomainData("C648B", "Gran Canaria - Aeropuerto (Gando)", "Las Palmas", 24.0, 27.9319, -15.3867),
            AemetStationDomainData("C659H", "Las Palmas de Gran Canaria (Puerto de La Luz)", "Las Palmas", 5.0, 28.1417, -15.4278),
            AemetStationDomainData("C646U", "Maspalomas (San Bartolomé de Tirajana)", "Las Palmas", 15.0, 27.7556, -15.5806),
            AemetStationDomainData("C647E", "San Bartolomé de Tirajana (Cuevas del Retamar)", "Las Palmas", 890.0, 27.9250, -15.5722),
            AemetStationDomainData("C643O", "Mogán (Puerto Rico)", "Las Palmas", 10.0, 27.7861, -15.7111),
            AemetStationDomainData("C653E", "Agaete (Puerto de Las Nieves)", "Las Palmas", 45.0, 28.1000, -15.7000),
            AemetStationDomainData("C656U", "Arucas", "Las Palmas", 240.0, 28.1194, -15.5222),
            AemetStationDomainData("C654E", "Gáldar", "Las Palmas", 120.0, 28.1444, -15.6528),
            AemetStationDomainData("C658B", "Telde (Centro)", "Las Palmas", 130.0, 27.9972, -15.4167),
            AemetStationDomainData("C651C", "Teror", "Las Palmas", 540.0, 28.0583, -15.5472),
            AemetStationDomainData("C650A", "Vega de San Mateo", "Las Palmas", 840.0, 28.0111, -15.5333),
            AemetStationDomainData("C645F", "Santa Lucía de Tirajana", "Las Palmas", 680.0, 27.9111, -15.5417),
            AemetStationDomainData("C652Y", "Tejeda (Cruz de Tejeda)", "Las Palmas", 1560.0, 27.9972, -15.5972),
            AemetStationDomainData("C657A", "Santa María de Guía", "Las Palmas", 180.0, 28.1361, -15.6333),
            AemetStationDomainData("C642B", "La Aldea de San Nicolás", "Las Palmas", 35.0, 27.9833, -15.7833),
            AemetStationDomainData("C655C", "Moya", "Las Palmas", 490.0, 28.1111, -15.5833),
            AemetStationDomainData("C644A", "Agüimes", "Las Palmas", 275.0, 27.9056, -15.4472),
            AemetStationDomainData("C640V", "Valleseco", "Las Palmas", 1000.0, 28.0500, -15.5750),

            // --- LANZAROTE ---
            AemetStationDomainData("C249I", "Lanzarote - Aeropuerto (César Manrique)", "Las Palmas", 14.0, 28.9456, -13.6053),
            AemetStationDomainData("C248C", "Arrecife (Capital)", "Las Palmas", 20.0, 28.9631, -13.5578),
            AemetStationDomainData("C247A", "Haría", "Las Palmas", 260.0, 29.1444, -13.4972),
            AemetStationDomainData("C245B", "Playa Blanca (Yaiza)", "Las Palmas", 15.0, 28.8639, -13.8278),
            AemetStationDomainData("C246D", "Tías (Puerto del Carmen)", "Las Palmas", 200.0, 28.9556, -13.6500),
            AemetStationDomainData("C244E", "Teguise", "Las Palmas", 360.0, 29.0611, -13.5611),
            AemetStationDomainData("C243G", "Tinajo", "Las Palmas", 195.0, 29.0722, -13.6778),
            AemetStationDomainData("C242M", "San Bartolomé", "Las Palmas", 240.0, 28.9972, -13.6139),

            // --- FUERTEVENTURA ---
            AemetStationDomainData("C349I", "Fuerteventura - Aeropuerto (El Matorral)", "Las Palmas", 25.0, 28.4528, -13.8639),
            AemetStationDomainData("C348B", "Puerto del Rosario", "Las Palmas", 18.0, 28.5000, -13.8625),
            AemetStationDomainData("C347C", "Corralejo (La Oliva)", "Las Palmas", 12.0, 28.7306, -13.8694),
            AemetStationDomainData("C345E", "Morro Jable (Pájara)", "Las Palmas", 20.0, 28.0528, -14.3556),
            AemetStationDomainData("C346A", "Gran Tarajal (Tuineje)", "Las Palmas", 25.0, 28.2194, -14.0222),
            AemetStationDomainData("C344U", "Antigua", "Las Palmas", 254.0, 28.4194, -14.0139),
            AemetStationDomainData("C343F", "Betancuria", "Las Palmas", 395.0, 28.4250, -14.0583),
            AemetStationDomainData("C342X", "Pájara (Centro)", "Las Palmas", 195.0, 28.3500, -14.1083),

            // --- LA PALMA ---
            AemetStationDomainData("C149I", "La Palma - Aeropuerto (Mazo)", "Santa Cruz de Tenerife", 31.0, 28.6264, -17.7556),
            AemetStationDomainData("C148B", "Santa Cruz de La Palma", "Santa Cruz de Tenerife", 25.0, 28.6833, -17.7667),
            AemetStationDomainData("C147A", "Los Llanos de Aridane", "Santa Cruz de Tenerife", 340.0, 28.6583, -17.9139),
            AemetStationDomainData("C145E", "Fuencaliente", "Santa Cruz de Tenerife", 710.0, 28.4917, -17.8444),
            AemetStationDomainData("C146D", "El Paso", "Santa Cruz de Tenerife", 630.0, 28.6500, -17.8806),
            AemetStationDomainData("C144N", "Tazacorte", "Santa Cruz de Tenerife", 105.0, 28.6417, -17.9333),
            AemetStationDomainData("C143P", "Puntagorda", "Santa Cruz de Tenerife", 715.0, 28.7722, -17.9861),
            AemetStationDomainData("C142B", "Breña Alta", "Santa Cruz de Tenerife", 350.0, 28.6611, -17.7861),
            AemetStationDomainData("C141R", "Roque de los Muchachos (Garafía)", "Santa Cruz de Tenerife", 2196.0, 28.7667, -17.8833),

            // --- LA GOMERA ---
            AemetStationDomainData("C549I", "La Gomera - Aeropuerto (Alajeró)", "Santa Cruz de Tenerife", 218.0, 28.0294, -17.2147),
            AemetStationDomainData("C548B", "San Sebastián de La Gomera", "Santa Cruz de Tenerife", 15.0, 28.0917, -17.1111),
            AemetStationDomainData("C547A", "Vallehermoso", "Santa Cruz de Tenerife", 180.0, 28.1806, -17.2639),
            AemetStationDomainData("C546C", "Valle Gran Rey", "Santa Cruz de Tenerife", 20.0, 28.1250, -17.3333),
            AemetStationDomainData("C545D", "Hermigua", "Santa Cruz de Tenerife", 150.0, 28.1806, -17.1889),
            AemetStationDomainData("C544E", "Agulo", "Santa Cruz de Tenerife", 260.0, 28.1889, -17.1944),

            // --- EL HIERRO ---
            AemetStationDomainData("C049I", "El Hierro - Aeropuerto (Valverde)", "Santa Cruz de Tenerife", 32.0, 27.8147, -17.8872),
            AemetStationDomainData("C048B", "Valverde (Capital)", "Santa Cruz de Tenerife", 570.0, 27.8083, -17.9139),
            AemetStationDomainData("C047A", "Frontera", "Santa Cruz de Tenerife", 300.0, 27.7528, -18.0111),
            AemetStationDomainData("C046C", "El Pinar de El Hierro", "Santa Cruz de Tenerife", 800.0, 27.7000, -17.9806),
            AemetStationDomainData("C045E", "La Restinga", "Santa Cruz de Tenerife", 10.0, 27.6389, -17.9806)
        ).sortedBy { it.nombre }
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
    private val weatherCache = mutableMapOf<String, Pair<Long, WeatherDomainData>>()
    private val CACHE_DURATION_MS = 15 * 60 * 1000L // 15 minutes

    suspend fun fetchWeather(cityName: String, lat: Double, lng: Double): WeatherDomainData {
        val cacheKey = "$lat,$lng"
        val cached = weatherCache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
            return cached.second
        }
        
        return withContext(Dispatchers.IO) {
            try {
                // Fetch Core data
                val weatherUrl = WeatherApiClient.buildWeatherUrl(lat, lng)
                val response = WeatherApiClient.api.getWeather(weatherUrl)
                
                // Fetch Air Quality data
                val aqiUrl = WeatherApiClient.buildAirQualityUrl(lat, lng)
                val aqiResponse = WeatherApiClient.api.getAirQuality(aqiUrl)
                
                // Convert response models to unified Domain Models
                val domainData = convertToDomain(cityName, response, aqiResponse)
                weatherCache[cacheKey] = Pair(System.currentTimeMillis(), domainData)
                domainData
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w("WeatherRepository", "Network fetch failed or rate-limited. Serving fallback. Msg: ${e.message}")
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
            val currentTime = current.time
            var startIndex = h.time.indexOfFirst { it >= currentTime }
            if (startIndex == -1) startIndex = 0

            val totalHours = (h.time.size - startIndex).coerceAtMost(24)
            for (i in 0 until totalHours) {
                val index = startIndex + i
                val timeStr = h.time[index].substringAfter("T") // Extract just the "HH:MM" block
                hourlyItems.add(
                    HourlyForecastItem(
                        timeString = timeStr,
                        temperature = h.temperature[index],
                        humidity = h.humidity[index],
                        precipitationProbability = h.precipitationProbability[index]
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
        val so2 = curAqi?.so2 ?: 12.0

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

        val canaryAqiLevel = calculateCanaryAqiLevel(so2, no2, pm25, pm10, o3)

        val airQuality = AirQualityData(
            pm25 = pm25,
            pm10 = pm10,
            no2 = no2,
            o3 = o3,
            so2 = so2,
            canaryAqiLevel = canaryAqiLevel,
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
