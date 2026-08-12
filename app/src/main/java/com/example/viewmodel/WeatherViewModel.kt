package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.data.*
import com.example.data.InfoPlayasFlag
import com.example.db.FavoriteCity
import com.example.db.FavoriteBeach
import com.example.repository.BeachRepository
import com.example.repository.CloudSyncManager
import com.example.repository.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherDomainData) : WeatherUiState
    data class Error(val errorMessage: String, val fallbackData: WeatherDomainData? = null) : WeatherUiState
}

sealed interface MarineUiState {
    object Idle : MarineUiState
    object Loading : MarineUiState
    data class Success(
        val data: MarineWeatherDto, 
        val tides: List<TideInfo>,
        val sunrise: String?,
        val sunset: String?,
        val liveFlag: InfoPlayasFlag? = null,
        val liveBeach: InfoPlayasBeach? = null
    ) : MarineUiState
    data class Error(val message: String) : MarineUiState
}

sealed interface AemetStationsUiState {
    object Idle : AemetStationsUiState
    object Loading : AemetStationsUiState
    data class Success(val stations: List<AemetStationDomainData>) : AemetStationsUiState
    data class Error(val message: String) : AemetStationsUiState
}

sealed interface WarningsUiState {
    object Idle : WarningsUiState
    object Loading : WarningsUiState
    data class Success(val warnings: List<AemetWarningDomainData>) : WarningsUiState
    data class Error(val message: String) : WarningsUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application)
    private val beachRepository = BeachRepository(AppDatabase.getDatabase(application, viewModelScope).beachDao())
    val cloudSync = CloudSyncManager(application)

    // UI state flows
    val favorites: StateFlow<List<FavoriteCity>> = repository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteBeaches: StateFlow<List<FavoriteBeach>> = repository.allFavoriteBeaches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCity = MutableStateFlow<FavoriteCity?>(null)
    val selectedCity: StateFlow<FavoriteCity?> = _selectedCity.asStateFlow()

    private val _actualLocation = MutableStateFlow<FavoriteCity?>(null)
    val actualLocation: StateFlow<FavoriteCity?> = _actualLocation.asStateFlow()

    private val _weatherUiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherUiState: StateFlow<WeatherUiState> = _weatherUiState.asStateFlow()

    // Beach flows
    val beaches: StateFlow<List<BeachPartial>> = beachRepository.getPartialBeaches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedBeach = MutableStateFlow<BeachEntity?>(null)
    val selectedBeach: StateFlow<BeachEntity?> = _selectedBeach.asStateFlow()

    private val _marineUiState = MutableStateFlow<MarineUiState>(MarineUiState.Idle)
    val marineUiState: StateFlow<MarineUiState> = _marineUiState.asStateFlow()

    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Regional alerts simulated from AEMET
    private val _aemetAlert = MutableStateFlow<String?>(null)
    val aemetAlert: StateFlow<String?> = _aemetAlert.asStateFlow()

    // AEMET Stations UI state
    private val _aemetStationsUiState = MutableStateFlow<AemetStationsUiState>(AemetStationsUiState.Idle)
    val aemetStationsUiState: StateFlow<AemetStationsUiState> = _aemetStationsUiState.asStateFlow()

    // AEMET Warnings UI state
    private val _warningsUiState = MutableStateFlow<WarningsUiState>(WarningsUiState.Idle)
    val warningsUiState: StateFlow<WarningsUiState> = _warningsUiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // SharedPreferences for island warning preferences
    private val sharedPrefs = application.getSharedPreferences("clima_canarias_prefs", Context.MODE_PRIVATE)

    private val _selectedIslands = MutableStateFlow<Set<String>>(
        sharedPrefs.getStringSet("selected_islands", emptySet()) ?: emptySet()
    )
    val selectedIslands: StateFlow<Set<String>> = _selectedIslands.asStateFlow()

    fun toggleIslandSelection(island: String) {
        val current = _selectedIslands.value.toMutableSet()
        if (current.contains(island)) {
            current.remove(island)
        } else {
            current.add(island)
        }
        sharedPrefs.edit().putStringSet("selected_islands", current).apply()
        _selectedIslands.value = current
        Log.d("WeatherViewModel", "Updated preferred islands: $current")
    }

    fun scheduleBackgroundAlertsCheck() {
        try {
            val workManager = WorkManager.getInstance(getApplication())
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<com.example.service.AemetWarningWorker>(
                15, TimeUnit.MINUTES // 15 minutes is the minimum allowed interval for WorkManager
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "aemet_alerts_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
            Log.d("WeatherViewModel", "AEMET Alerts periodic work scheduled successfully.")
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Failed to schedule background alerts check", e)
        }
    }

    init {
        // Initialize Room DB with predefined canary cities
        viewModelScope.launch {
            repository.initializePredefinedCitiesIfEmpty()
            
            // Listen to favorites list and set initial selection
            favorites.filter { it.isNotEmpty() }.first().let { list ->
                if (_selectedCity.value == null) {
                    val defaultCity = list.find { it.name.contains("Las Palmas") } ?: list.first()
                    selectCity(defaultCity)
                }
            }
        }
        scheduleBackgroundAlertsCheck()
    }

    fun selectBeachId(id: String) {
        viewModelScope.launch {
            val beachEntity = beachRepository.getBeachDetailsById(id)
            _selectedBeach.value = beachEntity
            fetchMarineWeatherForBeach(beachEntity)
        }
    }

    fun fetchMarineWeatherForBeach(beach: BeachEntity) {
        viewModelScope.launch {
            _marineUiState.value = MarineUiState.Loading
            val (data, tides) = repository.fetchMarineWeather(beach.lat, beach.lng)
            
            // Try to extract sunrise/sunset from the main weather state if it's already there
            val currentMainData = (_weatherUiState.value as? WeatherUiState.Success)?.data
            val sunrise = currentMainData?.sunrise ?: "07:07"
            val sunset = currentMainData?.sunset ?: "20:50"

            var liveFlag: InfoPlayasFlag? = null
            var liveBeach: InfoPlayasBeach? = null
            try {
                // Fetch info playas beach to get internal id
                val infoPlayasBeaches = WeatherApiClient.api.getInfoPlayasBeaches()
                val matchBeach = infoPlayasBeaches.data.find { beachObj ->
                    val rawDgse = beachObj.dgse
                    val dgseStr = when (rawDgse) {
                        is Number -> rawDgse.toLong().toString()
                        is String -> rawDgse.trim()
                        else -> rawDgse?.toString()?.trim()
                    }
                    dgseStr != null && dgseStr == beach.id.trim()
                }
                if (matchBeach != null) {
                    liveBeach = matchBeach
                    val flags = WeatherApiClient.api.getInfoPlayasFlags()
                    liveFlag = flags.data.find { it.beachLocationId == matchBeach.id }
                }
                Log.d("WeatherViewModel", "Beach match: ${matchBeach != null}, matchBeachId: ${matchBeach?.id}, liveFlagFound: ${liveFlag != null}, uvdb: ${liveFlag?.uvdb}")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Failed to fetch live flag", e)
            }

            if (data != null) {
                _marineUiState.value = MarineUiState.Success(data, tides, sunrise, sunset, liveFlag, liveBeach)
                try {
                    val wave = data.hourly?.waveHeight?.firstOrNull() ?: 1.2
                    val temp = liveFlag?.waterTemp ?: 21.5
                    val flagText = when (liveFlag?.flag) {
                        3 -> "🔴 Bandera Roja"
                        2 -> "🟡 Bandera Amarilla"
                        1 -> "🟢 Bandera Verde"
                        else -> "🟢 Bandera Verde"
                    }
                    val seaStateText = "Olas: ${String.format(java.util.Locale.US, "%.1f", wave)}m • Temp: ${String.format(java.util.Locale.US, "%.1f", temp)}°C"
                    val favoriteBeach = com.example.db.FavoriteBeach(id = beach.id, name = beach.nombre)
                    val windSpeedVal = liveFlag?.windSpeed?.toDoubleOrNull() ?: 18.0
                    val windDirVal = liveFlag?.windOrientation ?: "NNE (Alisio)"
                    com.example.widget.WidgetDataUpdater.saveFavoriteBeachInfo(
                        getApplication(),
                        favoriteBeach,
                        wave ?: 1.2,
                        temp,
                        flagText,
                        seaStateText,
                        windSpeedVal,
                        windDirVal
                    )
                } catch (e: Exception) {
                    Log.e("WeatherViewModel", "Error saving marine widget data", e)
                }
            } else {
                _marineUiState.value = MarineUiState.Error("No se pudieron cargar las condiciones marítimas.")
            }
        }
    }

    fun selectCity(city: FavoriteCity) {
        _selectedCity.value = city
        fetchWeatherForCity(city)
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _selectedCity.value?.let { city ->
                    val data = repository.fetchWeather(city.name, city.latitude, city.longitude)
                    _weatherUiState.value = WeatherUiState.Success(data)
                    evaluateAemetAlerts(data)
                } ?: run {
                    val defaultCity = favorites.value.firstOrNull()
                    if (defaultCity != null) {
                        selectCity(defaultCity)
                    }
                }
                _selectedBeach.value?.let { beach ->
                    fetchMarineWeatherForBeach(beach)
                }
                loadWarnings()
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Manual refresh error", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun fetchWeatherForCity(city: FavoriteCity) {
        viewModelScope.launch {
            _weatherUiState.value = WeatherUiState.Loading
            try {
                // Fetch unified data from Open-Meteo or fall back beautifully
                val data = repository.fetchWeather(city.name, city.latitude, city.longitude)
                _weatherUiState.value = WeatherUiState.Success(data)
                com.example.widget.WidgetDataUpdater.saveWeatherSummary(getApplication(), data)
                
                // Set AEMET warnings depending on data values
                evaluateAemetAlerts(data)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "UI weather fetch failed", e)
                _weatherUiState.value = WeatherUiState.Error("Fallo crítico recuperable de red. Mostrando datos locales.")
            }
        }
    }

    private fun evaluateAemetAlerts(data: WeatherDomainData) {
        // Calculate extreme weather alerts for Canary Islands representation
        _aemetAlert.value = when {
            data.temperatureCelsius >= 33.0 -> {
                "AVISO DE LUZ ROJA: Alerta por altas temperaturas en el archipiélago. Registrados ${String.format("%.1f", data.temperatureCelsius)}°C. Se insta a hidratarse constantemente y evitar exposición directa de 11:00 a 16:00."
            }
            data.airQuality?.calimaSeverity == CalimaSeverity.SEVERE -> {
                "ALERTA AMARILLA AEMET: Calima Severa sobre Canarias. Visibilidad reducida a menos de 1000m por partículas saharianas. PM10: ${data.airQuality.pm10} µg/m³."
            }
            data.windSpeedKmh >= 35.0 -> {
                "ALERTA POR VIENTOS FUERTES: Rachas de viento de noreste de hasta ${String.format("%.1f", data.windSpeedKmh)} km/h en vertientes expuestas canarias."
            }
            else -> null
        }
    }

        fun searchAndAddLocation(query: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val url = "https://geocoding-api.open-meteo.com/v1/search?name=${query}&count=5&language=es&format=json"
                val response = com.example.data.WeatherApiClient.api.searchLocation(url)
                val results = response.results
                if (results != null && results.isNotEmpty()) {
                    val first = results[0]
                    addCustomFavorite(first.name, first.latitude, first.longitude)
                    onResult(null) // Success
                } else {
                    onResult("No se encontraron resultados para '$query'")
                }
            } catch (e: Exception) {
                onResult("Error al buscar: ${e.message}")
            }
        }
    }

fun addCustomFavorite(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.addFavorite(name, latitude, longitude)
            // If we just added one, fetch it automatically
            val city = FavoriteCity(name = name, latitude = latitude, longitude = longitude)
            selectCity(city)
        }
    }

    fun fetchCurrentLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("WeatherViewModel", "Location permissions are not granted yet.")
            return
        }

        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d("WeatherViewModel", "Fetched last known location: ${location.latitude}, ${location.longitude}")
                        updateToCurrentLocation(location.latitude, location.longitude)
                    } else {
                        val cancellationTokenSource = CancellationTokenSource()
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cancellationTokenSource.token
                        ).addOnSuccessListener { newLocation ->
                            if (newLocation != null) {
                                Log.d("WeatherViewModel", "Fetched current location: ${newLocation.latitude}, ${newLocation.longitude}")
                                updateToCurrentLocation(newLocation.latitude, newLocation.longitude)
                            } else {
                                Log.e("WeatherViewModel", "Current location is null")
                                android.os.Handler(android.os.Looper.getMainLooper()).post { android.widget.Toast.makeText(context, "No se pudo obtener la ubicación actual. Comprueba el GPS.", android.widget.Toast.LENGTH_LONG).show() }
                            }
                        }.addOnFailureListener { e ->
                            Log.e("WeatherViewModel", "Error getting current location", e)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { android.widget.Toast.makeText(context, "Error al obtener la ubicación.", android.widget.Toast.LENGTH_LONG).show() }
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("WeatherViewModel", "Error getting last location", e)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception while fetching location", e)
            }
        }
    }

    private fun updateToCurrentLocation(lat: Double, lng: Double) {
        val locationCity = FavoriteCity(
            name = "Ubicación Actual",
            latitude = lat,
            longitude = lng,
            isPredefined = false
        )
        _actualLocation.value = locationCity
        selectCity(locationCity)
    }

    fun removeActualLocation() {
        _actualLocation.value = null
        if (_selectedCity.value?.name == "Ubicación Actual") {
            val list = favorites.value
            if (list.isNotEmpty()) {
                val defaultCity = list.find { it.isPredefined } ?: list.first()
                selectCity(defaultCity)
            } else {
                _selectedCity.value = null
            }
        }
    }

    fun removeFavorite(city: FavoriteCity) {
        if (city.name == "Ubicación Actual") {
            removeActualLocation()
            return
        }
        viewModelScope.launch {
            repository.removeFavorite(city)
            // If we removed currently selected, fall back to first
            if (_selectedCity.value?.name == city.name) {
                val remaining = favorites.value.filter { it.name != city.name }
                if (remaining.isNotEmpty()) {
                    selectCity(remaining.first())
                } else {
                    _selectedCity.value = null
                }
            }
        }
    }

    fun addFavoriteBeach(id: String, name: String) {
        viewModelScope.launch {
            repository.addFavoriteBeach(id, name)
        }
    }

    fun removeFavoriteBeach(beach: FavoriteBeach) {
        viewModelScope.launch {
            repository.removeFavoriteBeach(beach)
        }
    }

    fun restorePredefinedCities() {
        viewModelScope.launch {
            repository.restorePredefinedCities()
            // If nothing is selected, find the default from the restored ones
            val list = favorites.value
            if (_selectedCity.value == null && list.isNotEmpty()) {
                val defaultCity = list.find { it.name.contains("Las Palmas") } ?: list.first()
                selectCity(defaultCity)
            }
        }
    }

    fun toggleTemperatureUnit() {
        _isCelsius.value = !_isCelsius.value
        cloudSync.updateTemperatureUnitPref(_isCelsius.value)
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun triggerSaveToCloud() {
        val user = cloudSync.userProfile.value ?: return
        val currentCities = favorites.value
        val currentBeaches = favoriteBeaches.value
        cloudSync.saveToCloud(
            currentCities,
            currentBeaches,
            _isDarkTheme.value,
            _isCelsius.value
        )
    }

    fun triggerRestoreFromCloud() {
        val user = cloudSync.userProfile.value ?: return
        cloudSync.restoreFromCloud { cities, beaches, isDarkMode, isCelsius ->
            viewModelScope.launch {
                repository.replaceFavorites(cities)
                repository.replaceFavoriteBeaches(beaches)
                _isDarkTheme.value = isDarkMode
                _isCelsius.value = isCelsius
                _selectedCity.value?.let { fetchWeatherForCity(it) }
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            val localList = favorites.value
            cloudSync.syncWithCloud(
                localList,
                _isDarkTheme.value,
                _isCelsius.value
            ) { mergedList, cloudIsDarkMode, cloudIsCelsius ->
                // Sync complete: save back to local DB if required
                viewModelScope.launch {
                    repository.replaceFavorites(mergedList)
                    _isDarkTheme.value = cloudIsDarkMode
                    _isCelsius.value = cloudIsCelsius
                    // Refresh active forecast to capture sync updates
                    _selectedCity.value?.let { fetchWeatherForCity(it) }
                }
            }
        }
    }

    fun loadWarnings() {
        viewModelScope.launch {
            _warningsUiState.value = WarningsUiState.Loading
            try {
                // Pass a dummy or existing API key since ATOM feed doesn't require it
                val apiKey = com.example.security.AemetCredentialManager.getAemetApiKey()
                val warningsList = repository.fetchAemetWarnings(if (apiKey.isBlank()) "dummy" else apiKey)
                _warningsUiState.value = WarningsUiState.Success(warningsList)
                com.example.widget.WidgetDataUpdater.saveAemetWarnings(getApplication(), warningsList)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error loading AEMET warnings", e)
                _warningsUiState.value = WarningsUiState.Error("Error: ${e.message ?: "Desconocido"}")
            }
        }
    }

    fun loadAemetStations() {
        val apiKey = com.example.security.AemetCredentialManager.getAemetApiKey()
        if (apiKey.isBlank()) {
            _aemetStationsUiState.value = AemetStationsUiState.Error(
                "Clave de API de AEMET no configurada. Configure la AEMET_API_KEY en los Secrets de AI Studio."
            )
            return
        }

        viewModelScope.launch {
            _aemetStationsUiState.value = AemetStationsUiState.Loading
            try {
                val list = repository.fetchAemetStations(apiKey)
                if (list.isEmpty()) {
                    _aemetStationsUiState.value = AemetStationsUiState.Error(
                        "No hay estaciones disponibles."
                    )
                } else {
                    _aemetStationsUiState.value = AemetStationsUiState.Success(list)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error loading AEMET stations", e)
                _aemetStationsUiState.value = AemetStationsUiState.Error("Error: ${e.message ?: "Desconocido"}")
            }
        }
    }

    fun loadAemetStationObservation(stationIndicativo: String) {
        val apiKey = com.example.security.AemetCredentialManager.getAemetApiKey()
        if (apiKey.isBlank()) return

        val currentState = _aemetStationsUiState.value
        if (currentState !is AemetStationsUiState.Success) return

        // Set the station to loading observation
        val updatedStations = currentState.stations.map { station ->
            if (station.indicativo == stationIndicativo) {
                station.copy(isLoadingObservation = true, observationError = null)
            } else {
                station
            }
        }
        _aemetStationsUiState.value = AemetStationsUiState.Success(updatedStations)

        viewModelScope.launch {
            try {
                val obs = repository.fetchAemetStationObservation(apiKey, stationIndicativo)
                val freshState = _aemetStationsUiState.value
                if (freshState is AemetStationsUiState.Success) {
                    val finalStations = freshState.stations.map { station ->
                        if (station.indicativo == stationIndicativo) {
                            if (obs != null) {
                                val temp = obs.ta.toNullableDouble()
                                val hum = obs.hr.toNullableDouble()
                                val windV = obs.vv.toNullableDouble()
                                val windD = obs.dv.toNullableDouble()
                                val pres = obs.pres.toNullableDouble()
                                val prec = obs.prec.toNullableDouble()
                                val rachaMax = obs.vmax.toNullableDouble()
                                
                                station.copy(
                                    temperatura = temp,
                                    humedad = hum,
                                    vientoVelocidad = windV,
                                    vientoDireccion = windD,
                                    presion = pres,
                                    precipitacion = prec,
                                    racha = rachaMax,
                                    fechaObservacion = obs.fint,
                                    isLoadingObservation = false,
                                    observationError = null
                                )
                            } else {
                                station.copy(
                                    isLoadingObservation = false,
                                    observationError = "No hay datos recientes disponibles para esta estación."
                                )
                            }
                        } else {
                            station
                        }
                    }
                    _aemetStationsUiState.value = AemetStationsUiState.Success(finalStations)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error loading observation for $stationIndicativo", e)
                val freshState = _aemetStationsUiState.value
                if (freshState is AemetStationsUiState.Success) {
                    val finalStations = freshState.stations.map { station ->
                        if (station.indicativo == stationIndicativo) {
                            station.copy(
                                isLoadingObservation = false,
                                observationError = "Error: ${e.message}"
                            )
                        } else {
                            station
                        }
                    }
                    _aemetStationsUiState.value = AemetStationsUiState.Success(finalStations)
                }
            }
        }
    }

    private fun Any?.toNullableDouble(): Double? {
        return when (this) {
            is Number -> this.toDouble()
            is String -> this.toDoubleOrNull()
            else -> null
        }
    }
}
