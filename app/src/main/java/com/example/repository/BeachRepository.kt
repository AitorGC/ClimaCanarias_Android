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
                    if (tokens.size > 4) {
                        try {
                            val province = tokens.getOrNull(0)?.trim() ?: ""
                            val island = tokens.getOrNull(1)?.trim() ?: ""
                            val municipality = tokens.getOrNull(2)?.trim() ?: ""
                            val name = tokens.getOrNull(3)?.trim() ?: ""
                            
                            val riesgo = tokens.getOrNull(8)?.trim() ?: ""
                            val banderaAzul = tokens.getOrNull(14)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val accesoPmr = tokens.getOrNull(15)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val duchaAdaptada = tokens.getOrNull(16)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val aseoAdaptado = tokens.getOrNull(17)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val banoAsistido = tokens.getOrNull(18)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val sombraPmr = tokens.getOrNull(19)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val aparcar = tokens.getOrNull(20)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val aseos = tokens.getOrNull(21)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val lavapies = tokens.getOrNull(22)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val duchas = tokens.getOrNull(23)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val alquilerSombrillas = tokens.getOrNull(24)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val alquilerHamacas = tokens.getOrNull(25)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val alquilerNautico = tokens.getOrNull(26)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val areaInfantil = tokens.getOrNull(27)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            val areaDeportiva = tokens.getOrNull(28)?.trim()?.equals("Si", ignoreCase = true) ?: false
                            
                            val tipoArena = tokens.getOrNull(31)?.trim() ?: ""
                            val composition = tipoArena
                            val color = tokens.getOrNull(32)?.trim() ?: ""
                            
                            val condicionesBano = tokens.getOrNull(33)?.trim() ?: ""
                            val condicionesEntorno = tokens.getOrNull(34)?.trim() ?: ""
                            val condicionesAcceso = tokens.getOrNull(35)?.trim() ?: ""
                            
                            val latStr = tokens.getOrNull(37)?.trim() ?: ""
                            val lngStr = tokens.getOrNull(38)?.trim() ?: ""
                            
                            val lat = parseDms(latStr) ?: 0.0
                            val lng = parseDms(lngStr) ?: 0.0

                            // Añadimos la playa aunque no tenga coordenadas perfectas, para evitar que desaparezcan del dropdown.
                            if (name.isNotEmpty()) {
                                beaches.add(Beach(
                                    name = name, 
                                    island = island, 
                                    municipality = municipality, 
                                    province = province, 
                                    lat = lat, 
                                    lng = lng, 
                                    composition = composition,
                                    riesgo = riesgo,
                                    banderaAzul = banderaAzul,
                                    accesoPmr = accesoPmr,
                                    duchas = duchas,
                                    aparcar = aparcar,
                                    tipoArena = tipoArena,
                                    condicionesBano = condicionesBano,
                                    duchaAdaptada = duchaAdaptada,
                                    aseoAdaptado = aseoAdaptado,
                                    banoAsistido = banoAsistido,
                                    sombraPmr = sombraPmr,
                                    aseos = aseos,
                                    lavapies = lavapies,
                                    alquilerSombrillas = alquilerSombrillas,
                                    alquilerHamacas = alquilerHamacas,
                                    alquilerNautico = alquilerNautico,
                                    areaInfantil = areaInfantil,
                                    areaDeportiva = areaDeportiva,
                                    color = color,
                                    condicionesEntorno = condicionesEntorno,
                                    condicionesAcceso = condicionesAcceso
                                ))
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
