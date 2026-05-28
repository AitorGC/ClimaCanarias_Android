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
                    if (tokens.size > 38) {
                        try {
                            val name = tokens[1].trim()
                            val island = tokens[3].trim()
                            val municipality = tokens[2].trim()
                            val composition = tokens[31].trim()
                            // Fix extra quotes around coordinates
                            var latStr = tokens[37].replace("\"", "").trim()
                            var lngStr = tokens[38].replace("\"", "").trim()
                            val lat = latStr.toDoubleOrNull() ?: 0.0
                            val lng = lngStr.toDoubleOrNull() ?: 0.0

                            if (lat != 0.0 && lng != 0.0) {
                                beaches.add(Beach(name, island, municipality, lat, lng, composition))
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
}
