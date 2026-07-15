package com.example.repository

import android.content.Context
import android.util.Log
import com.example.db.FavoriteCity
import com.example.db.FavoriteBeach
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.GoogleAuthUtil
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class UserProfile(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)

class CloudSyncManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val driveClient = DriveApiClient(httpClient)

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
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            handleSignInResult(context, account)
        }
    }

    fun handleSignInResult(context: Context, account: GoogleSignInAccount) {
        _userProfile.value = UserProfile(
            email = account.email ?: "Unknown",
            displayName = account.displayName ?: "Unknown"
        )
    }

    fun logout() {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener {
            _userProfile.value = null
        }
    }

    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
        try {
            GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/drive.appdata")
        } catch (e: Exception) {
            Log.e("CloudSyncManager", "Failed to get token", e)
            null
        }
    }

    private val prefs = context.getSharedPreferences("cloud_mock_prefs", Context.MODE_PRIVATE)

    fun syncWithCloud(
        localFavorites: List<FavoriteCity>,
        localIsDarkMode: Boolean,
        localIsCelsius: Boolean,
        onSyncCompleted: (List<FavoriteCity>, Boolean, Boolean) -> Unit
    ) {
        if (_isSyncing.value) return
        scope.launch {
            _isSyncing.value = true
            val token = getAccessToken()
            if (token == null) {
                _isSyncing.value = false
                return@launch
            }

            val fileId = driveClient.getAppConfigFileId(token)
            var cloudDataString = ""
            if (fileId != null) {
                cloudDataString = driveClient.downloadAppConfig(token, fileId) ?: ""
            }

            val serverMockEntries = mutableListOf<FavoriteCity>()
            var cloudIsDarkMode = localIsDarkMode
            var cloudIsCelsius = localIsCelsius

            if (cloudDataString.isNotEmpty()) {
                try {
                    val root = JSONObject(cloudDataString)
                    cloudIsDarkMode = root.optBoolean("isDarkMode", localIsDarkMode)
                    cloudIsCelsius = root.optBoolean("isCelsius", localIsCelsius)
                    
                    val arr = root.optJSONArray("cities")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            serverMockEntries.add(
                                FavoriteCity(
                                    name = obj.getString("name"),
                                    latitude = obj.getDouble("latitude"),
                                    longitude = obj.getDouble("longitude"),
                                    isPredefined = obj.optBoolean("isPredefined", false),
                                    addedAt = obj.getLong("addedAt"),
                                    isSynced = true
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CloudSyncManager", "Parse error", e)
                }
            }

            val mergedList = ArrayList<FavoriteCity>()
            val allNames = (localFavorites.map { it.name } + serverMockEntries.map { it.name }).distinct()

            for (name in allNames) {
                val localItem = localFavorites.find { it.name == name }
                val serverItem = serverMockEntries.find { it.name == name }
                val resolved = when {
                    localItem != null && serverItem != null -> {
                        if (localItem.addedAt >= serverItem.addedAt) localItem.copy(isSynced = true) else serverItem
                    }
                    localItem != null -> localItem.copy(isSynced = true)
                    serverItem != null -> serverItem
                    else -> null
                }
                if (resolved != null) mergedList.add(resolved)
            }

            val root = JSONObject()
            root.put("isDarkMode", cloudIsDarkMode)
            root.put("isCelsius", cloudIsCelsius)
            val arr = JSONArray()
            mergedList.forEach {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("latitude", it.latitude)
                obj.put("longitude", it.longitude)
                obj.put("isPredefined", it.isPredefined)
                obj.put("addedAt", it.addedAt)
                arr.put(obj)
            }
            root.put("cities", arr)

            driveClient.uploadAppConfig(token, fileId, root.toString())

            _isDarkModePref.value = cloudIsDarkMode
            _isCelsiusPref.value = cloudIsCelsius
            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false

            withContext(Dispatchers.Main) {
                onSyncCompleted(mergedList, cloudIsDarkMode, cloudIsCelsius)
            }
        }
    }

    fun saveToCloud(
        localCities: List<FavoriteCity>,
        localBeaches: List<com.example.db.FavoriteBeach>,
        isDarkMode: Boolean,
        isCelsius: Boolean
    ) {
        scope.launch {
            _isSyncing.value = true
            val token = getAccessToken()
            if (token == null) {
                _isSyncing.value = false
                return@launch
            }

            val fileId = driveClient.getAppConfigFileId(token)

            val root = JSONObject()
            root.put("isDarkMode", isDarkMode)
            root.put("isCelsius", isCelsius)

            val arrCities = JSONArray()
            localCities.forEach {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("latitude", it.latitude)
                obj.put("longitude", it.longitude)
                obj.put("isPredefined", it.isPredefined)
                obj.put("addedAt", it.addedAt)
                arrCities.put(obj)
            }
            root.put("cities", arrCities)

            val arrBeaches = JSONArray()
            localBeaches.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("addedAt", it.addedAt)
                arrBeaches.put(obj)
            }
            root.put("beaches", arrBeaches)

            driveClient.uploadAppConfig(token, fileId, root.toString())

            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false
        }
    }

    fun restoreFromCloud(
        onRestoreCompleted: (List<FavoriteCity>, List<com.example.db.FavoriteBeach>, Boolean, Boolean) -> Unit
    ) {
        scope.launch {
            _isSyncing.value = true
            val token = getAccessToken()
            if (token == null) {
                _isSyncing.value = false
                return@launch
            }

            val fileId = driveClient.getAppConfigFileId(token)
            if (fileId == null) {
                _isSyncing.value = false
                return@launch
            }

            val cloudDataString = driveClient.downloadAppConfig(token, fileId)
            if (cloudDataString == null) {
                _isSyncing.value = false
                return@launch
            }

            val serverCities = mutableListOf<FavoriteCity>()
            val serverBeaches = mutableListOf<com.example.db.FavoriteBeach>()
            var isDarkMode = false
            var isCelsius = true

            try {
                val root = JSONObject(cloudDataString)
                isDarkMode = root.optBoolean("isDarkMode", false)
                isCelsius = root.optBoolean("isCelsius", true)

                val arrC = root.optJSONArray("cities")
                if (arrC != null) {
                    for (i in 0 until arrC.length()) {
                        val obj = arrC.getJSONObject(i)
                        serverCities.add(
                            FavoriteCity(
                                name = obj.getString("name"),
                                latitude = obj.getDouble("latitude"),
                                longitude = obj.getDouble("longitude"),
                                isPredefined = obj.optBoolean("isPredefined", false),
                                addedAt = obj.getLong("addedAt"),
                                isSynced = true
                            )
                        )
                    }
                }

                val arrB = root.optJSONArray("beaches")
                if (arrB != null) {
                    for (i in 0 until arrB.length()) {
                        val obj = arrB.getJSONObject(i)
                        serverBeaches.add(
                            FavoriteBeach(
                                id = obj.getString("id"),
                                name = obj.getString("name"),
                                addedAt = obj.getLong("addedAt")
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CloudSyncManager", "Parse error", e)
            }

            _isDarkModePref.value = isDarkMode
            _isCelsiusPref.value = isCelsius
            _lastSyncTime.value = System.currentTimeMillis()
            _isSyncing.value = false

            withContext(Dispatchers.Main) {
                onRestoreCompleted(serverCities, serverBeaches, isDarkMode, isCelsius)
            }
        }
    }

    fun updateTemperatureUnitPref(isCelsius: Boolean) {
        _isCelsiusPref.value = isCelsius
    }
}
