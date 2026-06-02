package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.InfoPlayasFlag
import com.example.db.FavoriteCity
import com.example.db.FavoriteBeach
import com.example.repository.BeachRepository
import com.example.repository.CloudSyncManager
import com.example.repository.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
            } else {
                _marineUiState.value = MarineUiState.Error("No se pudieron cargar las condiciones marítimas.")
            }
        }
    }

    fun selectCity(city: FavoriteCity) {
        _selectedCity.value = city
        fetchWeatherForCity(city)
    }

    fun fetchWeatherForCity(city: FavoriteCity) {
        viewModelScope.launch {
            _weatherUiState.value = WeatherUiState.Loading
            try {
                // Fetch unified data from Open-Meteo or fall back beautifully
                val data = repository.fetchWeather(city.name, city.latitude, city.longitude)
                _weatherUiState.value = WeatherUiState.Success(data)
                
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

    fun addCustomFavorite(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.addFavorite(name, latitude, longitude)
            // If we just added one, fetch it automatically
            val city = FavoriteCity(name = name, latitude = latitude, longitude = longitude)
            selectCity(city)
        }
    }

    fun removeFavorite(city: FavoriteCity) {
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
        cloudSync.saveToCloud(currentCities, currentBeaches)
    }

    fun triggerRestoreFromCloud() {
        val user = cloudSync.userProfile.value ?: return
        cloudSync.restoreFromCloud { cities, beaches ->
            viewModelScope.launch {
                repository.replaceFavorites(cities)
                repository.replaceFavoriteBeaches(beaches)
                _selectedCity.value?.let { fetchWeatherForCity(it) }
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            val localList = favorites.value
            cloudSync.syncWithCloud(localList) { mergedList ->
                // Sync complete: save back to local DB if required
                viewModelScope.launch {
                    repository.replaceFavorites(mergedList)
                    // Refresh active forecast to capture sync updates
                    _selectedCity.value?.let { fetchWeatherForCity(it) }
                }
            }
        }
    }
}
