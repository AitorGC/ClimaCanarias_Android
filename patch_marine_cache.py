with open("app/src/main/java/com/example/repository/WeatherRepository.kt", "r") as f:
    content = f.read()

target = """    suspend fun fetchMarineWeather(lat: Double, lng: Double): Pair<MarineWeatherDto?, List<TideInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val url = WeatherApiClient.buildMarineWeatherUrl(lat, lng)"""

replacement = """    private val marineCache = mutableMapOf<String, Pair<Long, Pair<MarineWeatherDto?, List<TideInfo>>>>()

    suspend fun fetchMarineWeather(lat: Double, lng: Double): Pair<MarineWeatherDto?, List<TideInfo>> {
        val cacheKey = "$lat,$lng"
        val cached = marineCache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
            return cached.second
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = WeatherApiClient.buildMarineWeatherUrl(lat, lng)"""

if target in content:
    content = content.replace(target, replacement)
    
    target2 = """                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Log.e("WeatherRepository", "Error fetching tide data", e)
                }
                Pair(marineData, tides)
            } catch (e: Exception) {"""
            
    replacement2 = """                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Log.e("WeatherRepository", "Error fetching tide data", e)
                }
                val result = Pair(marineData, tides)
                marineCache[cacheKey] = Pair(System.currentTimeMillis(), result)
                result
            } catch (e: Exception) {"""
            
    content = content.replace(target2, replacement2)
    with open("app/src/main/java/com/example/repository/WeatherRepository.kt", "w") as f:
        f.write(content)
    print("Marine Cache patched!")
else:
    print("Not found!")
