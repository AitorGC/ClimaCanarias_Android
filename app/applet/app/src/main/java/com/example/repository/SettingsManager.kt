package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AllergySettings(
    val allergyGrass: Boolean = false,
    val allergyOlive: Boolean = false,
    val allergyMugwort: Boolean = false,
    val allergyAlder: Boolean = false,
    val allergyBirch: Boolean = false,
    val allergyRagweed: Boolean = false,
    val sensitiveToDust: Boolean = false,
    val warningThreshold: Int = 1 // 1: Low, 2: Moderate, 3: High
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("allergy_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AllergySettings> = _settings.asStateFlow()

    private fun loadSettings(): AllergySettings {
        return AllergySettings(
            allergyGrass = prefs.getBoolean("allergy_grass", false),
            allergyOlive = prefs.getBoolean("allergy_olive", false),
            allergyMugwort = prefs.getBoolean("allergy_mugwort", false),
            allergyAlder = prefs.getBoolean("allergy_alder", false),
            allergyBirch = prefs.getBoolean("allergy_birch", false),
            allergyRagweed = prefs.getBoolean("allergy_ragweed", false),
            sensitiveToDust = prefs.getBoolean("sensitive_dust", false),
            warningThreshold = prefs.getInt("warning_threshold", 1)
        )
    }

    fun saveSettings(newSettings: AllergySettings) {
        prefs.edit().apply {
            putBoolean("allergy_grass", newSettings.allergyGrass)
            putBoolean("allergy_olive", newSettings.allergyOlive)
            putBoolean("allergy_mugwort", newSettings.allergyMugwort)
            putBoolean("allergy_alder", newSettings.allergyAlder)
            putBoolean("allergy_birch", newSettings.allergyBirch)
            putBoolean("allergy_ragweed", newSettings.allergyRagweed)
            putBoolean("sensitive_dust", newSettings.sensitiveToDust)
            putInt("warning_threshold", newSettings.warningThreshold)
        }.apply()
        _settings.value = newSettings
    }
}
