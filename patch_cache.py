with open("app/src/main/java/com/example/repository/WeatherRepository.kt", "r") as f:
    content = f.read()

target = """    // Main weather resolver
    suspend fun fetchWeather(cityName: String, lat: Double, lng: Double): WeatherDomainData {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch Core data
                val weatherUrl = WeatherApiClient.buildWeatherUrl(lat, lng)"""

replacement = """    // Main weather resolver
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
                val weatherUrl = WeatherApiClient.buildWeatherUrl(lat, lng)"""

if target in content:
    content = content.replace(target, replacement)
    
    target2 = """                // Convert response models to unified Domain Models
                convertToDomain(cityName, response, aqiResponse)
            } catch (e: Exception) {"""
            
    replacement2 = """                // Convert response models to unified Domain Models
                val domainData = convertToDomain(cityName, response, aqiResponse)
                weatherCache[cacheKey] = Pair(System.currentTimeMillis(), domainData)
                domainData
            } catch (e: Exception) {"""
            
    content = content.replace(target2, replacement2)
    with open("app/src/main/java/com/example/repository/WeatherRepository.kt", "w") as f:
        f.write(content)
    print("Cache patched!")
else:
    print("Not found!")
