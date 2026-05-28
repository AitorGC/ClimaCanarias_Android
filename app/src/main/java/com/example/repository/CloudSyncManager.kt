package com.example.repository

import android.content.Context
import android.util.Log
import com.example.db.FavoriteCity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)

class CloudSyncManager(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // UI visible States
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _isDarkModePref = MutableStateFlow<Boolean?>(null)
    val isDarkModePref: StateFlow<Boolean?> = _isDarkModePref.asStateFlow()

    private val _isCelsiusPref = MutableStateFlow(true)
    val isCelsiusPref: StateFlow<Boolean> = _isCelsiusPref.asStateFlow()

    init {
        // Attempt silent, passive sign-in on boot to provide seamless sync
        signInSilently()
    }

    fun signInSilently() {
        scope.launch {
            _isSyncing.value = true
            delay(1200) // Realistic passive network handshake simulation
            
            // Log in as user AitoR.GC89@gmail.com, simulating silent OAuth resolution
            _userProfile.value = UserProfile(
                email = "AitoR.GC89@gmail.com",
                displayName = "Aitor Santana"
            )
            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false
            Log.d("CloudSyncManager", "Silent passive Google login succeeded as Aitor Santana")
        }
    }

    fun triggerManualGoogleSignIn(email: String = "AitoR.GC89@gmail.com", name: String = "Aitor Santana") {
        scope.launch {
            _isSyncing.value = true
            delay(1500)
            _userProfile.value = UserProfile(email = email, displayName = name)
            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    fun logout() {
        scope.launch {
            _isSyncing.value = true
            delay(600)
            _userProfile.value = null
            _isSyncing.value = false
        }
    }

    private val prefs = context.getSharedPreferences("cloud_mock_prefs", Context.MODE_PRIVATE)

    // Bi-directional synchronization resolving conflicts using latest timestamps
    fun syncWithCloud(
        localFavorites: List<FavoriteCity>,
        onSyncCompleted: (List<FavoriteCity>) -> Unit
    ) {
        val user = _userProfile.value ?: return
        if (_isSyncing.value) return // Prevent overlapping sync conflicts

        scope.launch {
            _isSyncing.value = true
            Log.d("CloudSyncManager", "Starting bi-directional Firestore synchronization based on server timestamps...")
            
            // Simulating cloud server connection delay
            delay(1800)

            val systemTime = System.currentTimeMillis()
            
            // Load mock cloud entries from SharedPreferences
            val cloudDataString = prefs.getString("mock_cloud_favorites", "") ?: ""
            val serverMockEntries = mutableListOf<FavoriteCity>()
            
            if (cloudDataString.isNotEmpty()) {
                try {
                    val entries = cloudDataString.split("|||")
                    for (entry in entries) {
                        if (entry.isBlank()) continue
                        val parts = entry.split(":::")
                        if (parts.size == 5) {
                            serverMockEntries.add(
                                FavoriteCity(
                                    name = parts[0],
                                    latitude = parts[1].toDouble(),
                                    longitude = parts[2].toDouble(),
                                    isPredefined = false,
                                    addedAt = parts[3].toLong(),
                                    isSynced = true
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CloudSyncManager", "Error parsing mock cloud data", e)
                }
            }

            // Merge local and cloud list cleanly using timestamps
            val mergedList = ArrayList<FavoriteCity>()
            val allNames = (localFavorites.map { it.name } + serverMockEntries.map { it.name }).distinct()
            
            for (name in allNames) {
                val localItem = localFavorites.find { it.name == name }
                val serverItem = serverMockEntries.find { it.name == name }

                val resolved = when {
                    localItem != null && serverItem != null -> {
                        if (localItem.addedAt >= serverItem.addedAt) {
                            localItem.copy(isSynced = true)
                        } else {
                            serverItem
                        }
                    }
                    localItem != null -> localItem.copy(isSynced = true)
                    serverItem != null -> serverItem
                    else -> null
                }
                if (resolved != null) {
                    mergedList.add(resolved)
                }
            }
            
            // Save merged list back to "Cloud" (SharedPreferences)
            val updatedCloudString = mergedList.joinToString("|||") { 
                "${it.name}:::${it.latitude}:::${it.longitude}:::${it.addedAt}:::true"
            }
            prefs.edit().putString("mock_cloud_favorites", updatedCloudString).apply()

            // Sync Preference States
            _isCelsiusPref.value = true // Simulated Cloud fetch
            _lastSyncTime.value = systemTime
            _isSyncing.value = false
            
            withContext(Dispatchers.Main) {
                onSyncCompleted(mergedList)
            }
            Log.d("CloudSyncManager", "Bi-directional sync complete. Timestamp: $systemTime")
        }
    }

    fun updateTemperatureUnitPref(isCelsius: Boolean) {
        _isCelsiusPref.value = isCelsius
    }
}
