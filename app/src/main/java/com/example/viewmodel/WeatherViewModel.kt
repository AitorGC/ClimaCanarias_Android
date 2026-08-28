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
        val liveBeach: InfoPlayasBeach? = null,
        val weatherData: WeatherDomainData? = null
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


    private val _isAutoDarkMode = MutableStateFlow(sharedPrefs.getBoolean("auto_dark_mode", false))
    val isAutoDarkMode: StateFlow<Boolean> = _isAutoDarkMode.asStateFlow()

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

    private fun updateThemeForAutoMode() {
        if (_isAutoDarkMode.value) {
            val state = _weatherUiState.value
            if (state is WeatherUiState.Success) {
                val data = state.data
                val sunrise = data.sunrise ?: "07:00"
                val sunset = data.sunset ?: "20:00"
                
                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                val nowStr = sdf.format(java.util.Date())
                
                // Compare times lexicographically since format is HH:mm (24h)
                val isNight = nowStr < sunrise || nowStr > sunset
                if (_isDarkTheme.value != isNight) {
                    _isDarkTheme.value = isNight
                }
            } else {
                // Fallback: 20:00 to 07:00 is night
                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                val nowStr = sdf.format(java.util.Date())
                val isNight = nowStr < "07:00" || nowStr > "20:00"
                if (_isDarkTheme.value != isNight) {
                    _isDarkTheme.value = isNight
                }
            }
        }
    }

    init {
        ApiStatsTracker.init(application)
        // Initialize Room DB with predefined canary cities
        viewModelScope.launch {
            repository.initializePredefinedCitiesIfEmpty()
            
            // Listen to favorites list and set initial selection
            favorites.filter { it.isNotEmpty() }.first().let { list ->
                if (_selectedCity.value == null) {
                    val lastSelectedCityName = sharedPrefs.getString("last_selected_city", null)
                    val defaultCity = if (lastSelectedCityName != null) {
                        list.find { it.name == lastSelectedCityName } ?: list.first()
                    } else {
                        list.find { it.name.contains("Las Palmas") } ?: list.first()
                    }
                    selectCity(defaultCity)
                }
            }
        }
        viewModelScope.launch {
            _weatherUiState.collect {
                updateThemeForAutoMode()
            }
        }
        scheduleBackgroundAlertsCheck()
    }

    fun scheduleBackgroundAlertsCheck() {
        try {
            val workManager = WorkManager.getInstance(getApplication())
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<com.example.service.AemetWarningWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "AemetAlertsCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            Log.e("WeatherViewModel", "Failed to schedule background alerts check", e)
        }
    }

    fun clearSelectedBeach() {
        _selectedBeach.value = null
        _marineUiState.value = MarineUiState.Idle
    }

    fun selectBeachId(id: String) {
        viewModelScope.launch {
            val beachEntity = beachRepository.getBeachDetailsById(id)
            _selectedBeach.value = beachEntity
            fetchMarineWeatherForBeach(beachEntity)
        }
    }
    
    suspend fun fetchWeatherForBeach(lat: Double, lng: Double): WeatherDomainData? {
        return try {
            repository.fetchWeather("Playa", lat, lng)
        } catch (e: Exception) {
            null
        }
    }

    fun fetchMarineWeatherForBeach(beach: BeachEntity) {
        viewModelScope.launch {
            _marineUiState.value = MarineUiState.Loading
            try {
                val (data, tides) = repository.fetchMarineWeather(beach.lat, beach.lng)
                val weatherData = fetchWeatherForBeach(beach.lat, beach.lng)
                
                // Try to extract sunrise/sunset from the main weather state if it's already there
                val currentMainData = (_weatherUiState.value as? WeatherUiState.Success)?.data
                val sunrise = weatherData?.sunrise ?: currentMainData?.sunrise ?: "07:07"
                val sunset = weatherData?.sunset ?: currentMainData?.sunset ?: "20:50"

                var liveFlag: InfoPlayasFlag? = null
                var liveBeach: InfoPlayasBeach? = null
                try {
                    val beachResponse = com.example.data.WeatherApiClient.api.getInfoPlayasBeaches()
                    val beachData = beachResponse.data.firstOrNull { it.name?.contains(beach.nombre, ignoreCase = true) == true }
                    if (beachData != null) {
                        liveBeach = beachData
                        val flagResponse = com.example.data.WeatherApiClient.api.getInfoPlayasFlags()
                        liveFlag = flagResponse.data.find { it.beachLocationId == beachData.id }
                    }
                } catch (e: Exception) {
                    Log.e("WeatherVM", "Failed to fetch live flags", e)
                }
                
                if (data != null) {
                    _marineUiState.value = MarineUiState.Success(data, tides, sunrise, sunset, liveFlag, liveBeach, weatherData)
                } else {
                    _marineUiState.value = MarineUiState.Error("No se pudieron obtener los datos marítimos")
                }
            } catch (e: Exception) {
                Log.e("WeatherVM", "Marine Error", e)
                _marineUiState.value = MarineUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun selectCity(city: FavoriteCity) {
        _selectedCity.value = city
        sharedPrefs.edit().putString("last_selected_city", city.name).apply()
        fetchWeatherForCity(city)
    }

    fun fetchWeatherForCity(city: FavoriteCity) {
        viewModelScope.launch {
            _weatherUiState.value = WeatherUiState.Loading
            _aemetAlert.value = null
            try {
                val data = repository.fetchWeather(city.name, city.latitude, city.longitude)
                _weatherUiState.value = WeatherUiState.Success(data)
                com.example.widget.WidgetDataUpdater.saveWeatherSummary(getApplication(), data)
                evaluateAemetAlerts(data)
            } catch (e: Exception) {
                Log.e("WeatherVM", "Weather fetch error", e)
                _weatherUiState.value = WeatherUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _selectedCity.value?.let { fetchWeatherForCity(it) }
            _selectedBeach.value?.let { fetchMarineWeatherForBeach(it) }
            _isRefreshing.value = false
        }
    }

    fun fetchCurrentLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        viewModelScope.launch {
                            val city = FavoriteCity(
                                id = -1,
                                name = "Ubicación Actual",
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                            _actualLocation.value = city
                            selectCity(city)
                        }
                    }
                }
        }
    }

    fun removeActualLocation() {
        _actualLocation.value = null
        val defaultCity = favorites.value.firstOrNull()
        if (defaultCity != null) {
            selectCity(defaultCity)
        }
    }

    fun searchAndAddLocation(query: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val results = com.example.data.WeatherApiClient.api.searchLocation(query)
                val first = results.results?.firstOrNull()
                if (first != null) {
                    repository.addFavorite(first.name, first.latitude, first.longitude)
                    onResult(null)
                } else {
                    onResult("No se encontraron resultados")
                }
            } catch (e: Exception) {
                onResult("Error: ${e.message}")
            }
        }
    }

    fun addCustomFavorite(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.addFavorite(name, latitude, longitude)
        }
    }

    fun removeFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            repository.removeFavorite(city)
            if (_selectedCity.value == city) {
                favorites.value.firstOrNull { it != city }?.let { selectCity(it) }
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
        }
    }

    fun toggleTemperatureUnit() {
        _isCelsius.value = !_isCelsius.value
        cloudSync.updateTemperatureUnitPref(_isCelsius.value)
    }

    fun toggleAutoDarkMode() {
        _isAutoDarkMode.value = !_isAutoDarkMode.value
        sharedPrefs.edit().putBoolean("auto_dark_mode", _isAutoDarkMode.value).apply()
        updateThemeForAutoMode()
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun triggerSaveToCloud() {
        cloudSync.saveToCloud(
            favorites.value,
            favoriteBeaches.value,
            _isDarkTheme.value,
            _isCelsius.value
        )
    }

    fun triggerRestoreFromCloud() {
        cloudSync.restoreFromCloud { cities, beaches, isDarkMode, isCelsius ->
            viewModelScope.launch {
                repository.replaceFavorites(cities)
                repository.replaceFavoriteBeaches(beaches)
                _isDarkTheme.value = isDarkMode
                _isCelsius.value = isCelsius
            }
        }
    }

    fun triggerSync() {
        val user = cloudSync.userProfile.value ?: return
        viewModelScope.launch {
            val count = repository.getCount()
            if (count == 0) {
                triggerRestoreFromCloud()
            } else {
                cloudSync.restoreFromCloud { remoteCities, remoteBeaches, cloudIsDarkMode, cloudIsCelsius ->
                    viewModelScope.launch {
                        val currentCities = favorites.value
                        val mergedList = (currentCities + remoteCities).distinctBy { it.name }
                        repository.replaceFavorites(mergedList)
                        
                        val currentBeaches = favoriteBeaches.value
                        val mergedBeaches = (currentBeaches + remoteBeaches).distinctBy { it.id }
                        repository.replaceFavoriteBeaches(mergedBeaches)

                        _isDarkTheme.value = cloudIsDarkMode
                        _isCelsius.value = cloudIsCelsius
                        cloudSync.saveToCloud(mergedList, mergedBeaches, cloudIsDarkMode, cloudIsCelsius)
                    }
                }
            }
        }
    }

    fun loadWarnings() {
        viewModelScope.launch {
            _warningsUiState.value = WarningsUiState.Loading
            try {
                val apiKey = com.example.security.AemetCredentialManager.getAemetApiKey()
                val warningsList = repository.fetchAemetWarnings(apiKey)
                _warningsUiState.value = WarningsUiState.Success(warningsList)
            } catch (e: Exception) {
                _warningsUiState.value = WarningsUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun loadAemetStations() {
        viewModelScope.launch {
            _aemetStationsUiState.value = AemetStationsUiState.Loading
            try {
                val apiKey = com.example.security.AemetCredentialManager.getAemetApiKey()
                val list = repository.fetchAemetStations(apiKey)
                _aemetStationsUiState.value = AemetStationsUiState.Success(list)
            } catch (e: Exception) {
                _aemetStationsUiState.value = AemetStationsUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun loadAemetStationObservation(stationIndicativo: String) {
        viewModelScope.launch {
            val currentState = _aemetStationsUiState.value
            val targetStation = if (currentState is AemetStationsUiState.Success) {
                currentState.stations.find { it.indicativo == stationIndicativo }
            } else null

            // Mark station as loading observation
            if (currentState is AemetStationsUiState.Success) {
                _aemetStationsUiState.value = AemetStationsUiState.Success(
                    currentState.stations.map {
                        if (it.indicativo == stationIndicativo) it.copy(isLoadingObservation = true, observationError = null) else it
                    }
                )
            }

            try {
                val apiKey = com.example.security.AemetCredentialManager.getAemetApiKey()
                val obs = repository.fetchAemetStationObservation(
                    apiKey = apiKey,
                    indicativo = stationIndicativo,
                    stationLat = targetStation?.latitud ?: 0.0,
                    stationLon = targetStation?.longitud ?: 0.0
                )
                val refreshedState = _aemetStationsUiState.value
                if (refreshedState is AemetStationsUiState.Success && obs != null) {
                    val updatedList = refreshedState.stations.map {
                        if (it.indicativo == stationIndicativo) {
                            it.copy(
                                isLoadingObservation = false,
                                temperatura = obs.ta?.toString()?.toDoubleOrNull(),
                                humedad = obs.hr?.toString()?.toDoubleOrNull(),
                                vientoVelocidad = obs.vv?.toString()?.toDoubleOrNull(),
                                vientoDireccion = obs.dv?.toString()?.toDoubleOrNull(),
                                presion = obs.pres?.toString()?.toDoubleOrNull(),
                                precipitacion = obs.prec?.toString()?.toDoubleOrNull(),
                                racha = obs.vmax?.toString()?.toDoubleOrNull(),
                                fechaObservacion = obs.fint,
                                observationError = null
                            )
                        } else it
                    }
                    _aemetStationsUiState.value = AemetStationsUiState.Success(updatedList)
                } else if (refreshedState is AemetStationsUiState.Success) {
                    val updatedList = refreshedState.stations.map {
                        if (it.indicativo == stationIndicativo) {
                            it.copy(
                                isLoadingObservation = false,
                                observationError = "No hay datos de telemetría recientes disponibles."
                            )
                        } else it
                    }
                    _aemetStationsUiState.value = AemetStationsUiState.Success(updatedList)
                }
            } catch (e: Exception) {
                val refreshedState = _aemetStationsUiState.value
                if (refreshedState is AemetStationsUiState.Success) {
                    val updatedList = refreshedState.stations.map {
                        if (it.indicativo == stationIndicativo) {
                            it.copy(
                                isLoadingObservation = false,
                                observationError = "Error al obtener telemetría: ${e.message}"
                            )
                        } else it
                    }
                    _aemetStationsUiState.value = AemetStationsUiState.Success(updatedList)
                }
            }
        }
    }

    val apiStatsSummary: StateFlow<GlobalApiSummary> = ApiStatsTracker.statsFlow

    fun resetApiStats() {
        ApiStatsTracker.resetAllStats()
    }

    private fun evaluateAemetAlerts(data: WeatherDomainData) {
        if (data.temperatureCelsius > 34.0) {
            _aemetAlert.value = "ALERTA ROJA: Temperaturas excepcionalmente altas detectadas. Extreme las precauciones."
        } else if (data.temperatureCelsius > 30.0) {
            _aemetAlert.value = "AVISO AMARILLO: Temperaturas altas. Manténgase hidratado."
        } else if (data.windSpeedKmh > 70.0) {
            _aemetAlert.value = "AVISO NARANJA: Rachas de viento muy fuertes. Peligro en zonas expuestas."
        } else {
            _aemetAlert.value = null
        }
    }

    private fun isWarningActive(warning: AemetWarningDomainData): Boolean {
        // Just a stub since we are rewriting
        return true
    }
}
