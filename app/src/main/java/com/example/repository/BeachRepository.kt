package com.example.repository

import android.content.Context
import android.util.Log
import com.example.data.Beach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class BeachRepository(private val context: Context) {

    suspend fun getBeaches(): List<Beach> = withContext(Dispatchers.IO) {
        val beaches = mutableListOf<Beach>()
        try {
            val inputStream = context.assets.open("playas.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            // Skip header
            reader.readLine()

            var line: String? = reader.readLine()
            while (line != null) {
                if (line.isNotBlank()) {
                    // Csv parser
                    // 1: Nombre Playa, 3: Isla, 37: Latitud, 38: Longitud (0 indexed)
                    val tokens = line.split(";")
                    if (tokens.size > 37) {
                        try {
                            val name = tokens[1].trim()
                            val island = tokens[3].trim()
                            val municipality = tokens[2].trim()
                            val province = tokens[4].trim()
                            val composition = tokens[31].trim()
                            
                            val lat = parseDms(tokens[36]) ?: 0.0
                            val lng = parseDms(tokens[37]) ?: 0.0

                            if (lat != 0.0 && lng != 0.0) {
                                beaches.add(Beach(name, island, municipality, province, lat, lng, composition))
                            }
                        } catch (e: Exception) {
                            Log.e("BeachRepository", "Error parsing line: $line", e)
                        }
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e("BeachRepository", "Error reading beaches", e)
        }
        beaches
    }

    private fun parseDms(dmsStr: String): Double? {
        if (dmsStr.isBlank()) return null
        try {
            val trimmed = dmsStr.trim()
            // Check if it's already a clean decimal number
            trimmed.toDoubleOrNull()?.let { return it }

            var clean = trimmed.replace("\"", "").trim()
            val isNegative = clean.contains("W", ignoreCase = true) || clean.contains("S", ignoreCase = true)
            
            // Remove directional letters
            clean = clean.replace("[NWSEnwse]".toRegex(), "").trim()
            
            // Normalize degrees, minutes, seconds symbols to spaces
            clean = clean.replace("°", " ")
                         .replace("º", " ")
                         .replace("'", " ")
                         .replace(",", ".")
            
            // Split by whitespaces to get parts
            val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
            if (parts.isEmpty()) return null
            
            val degrees = Math.abs(parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0)
            val minutes = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
            val seconds = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
            
            var decimal = degrees + (minutes / 60.0) + (seconds / 3600.0)
            if (isNegative || trimmed.contains("-")) {
                decimal = -decimal
            }
            return decimal
        } catch (e: Exception) {
            Log.e("BeachRepository", "Error parsing DMS coordinate: $dmsStr", e)
            return null
        }
    }
}
