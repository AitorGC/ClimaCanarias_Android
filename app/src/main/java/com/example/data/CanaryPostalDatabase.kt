package com.example.data

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer

data class CanaryPostalLocation(
    val postalCode: String,
    val localidad: String,
    val provincia: String,
    val municipio: String,
    val latitude: Double,
    val longitude: Double
)

object CanaryPostalDatabase {
    private var locations: List<CanaryPostalLocation> = emptyList()
    private var isLoaded = false

    @Synchronized
    fun initialize(context: Context) {
        if (isLoaded) return

        val parsedList = mutableListOf<CanaryPostalLocation>()
        try {
            val inputStream = try {
                context.assets.open("codigos-postales.csv")
            } catch (e: Exception) {
                try {
                    context.resources.assets.open("codigos-postales.csv")
                } catch (e2: Exception) {
                    java.io.File(context.filesDir.parentFile, "app/src/main/res/codigos-postales.csv").inputStream()
                }
            }

            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                // Skip header: CODIGO POSTAL;LOCALIDAD;PROVINCIA;MUNICIPIO;SUBDIVISION;LATITUD;LONGITUD;EXACTITUD
                var line = reader.readLine()
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.isBlank()) continue
                    val parts = currentLine.split(";")
                    if (parts.size >= 7) {
                        val cp = parts[0].trim()
                        val localidad = parts[1].trim()
                        val provincia = parts[2].trim()
                        val municipio = parts[3].trim()
                        val lat = parseCoord(parts[5])
                        val lon = parseCoord(parts[6])

                        if (cp.isNotBlank() && localidad.isNotBlank() && lat != 0.0 && lon != 0.0) {
                            parsedList.add(
                                CanaryPostalLocation(
                                    postalCode = cp,
                                    localidad = localidad,
                                    provincia = provincia,
                                    municipio = municipio,
                                    latitude = lat,
                                    longitude = lon
                                )
                            )
                        }
                    }
                }
            }
            locations = parsedList
            isLoaded = true
            Log.d("CanaryPostalDatabase", "Cargadas ${locations.size} localidades canarias desde CSV")
        } catch (e: Exception) {
            Log.e("CanaryPostalDatabase", "Error cargando codigos-postales.csv", e)
        }
    }

    private fun parseCoord(str: String): Double {
        val clean = str.trim().replace(",", ".")
        val raw = clean.toDoubleOrNull() ?: return 0.0
        val isNeg = clean.startsWith("-")
        val digits = clean.filter { it.isDigit() }
        if (digits.length >= 4) {
            val whole = digits.substring(0, 2)
            val frac = digits.substring(2)
            val num = "$whole.$frac".toDoubleOrNull() ?: raw
            return if (isNeg) -num else num
        }
        return raw
    }

    private fun normalize(str: String): String {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    fun search(query: String, limit: Int = 30): List<CanaryPostalLocation> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()

        val isNumeric = q.all { it.isDigit() }
        if (isNumeric) {
            // Si coincide exactamente el código postal completo (ej. 35018)
            val exactMatches = locations.filter { it.postalCode == q }
            if (exactMatches.isNotEmpty()) {
                return exactMatches.take(limit)
            }
            // Si son 2 o más dígitos (ej. 35, 350)
            if (q.length >= 2) {
                return locations.filter { it.postalCode.startsWith(q) }.take(limit)
            }
            return emptyList()
        }

        // Búsqueda por texto (localidad o municipio)
        val normQuery = normalize(q)
        if (normQuery.length < 2) return emptyList()

        val exactOrPrefix = mutableListOf<CanaryPostalLocation>()
        val containsLoc = mutableListOf<CanaryPostalLocation>()
        val containsMun = mutableListOf<CanaryPostalLocation>()

        for (loc in locations) {
            val normLoc = normalize(loc.localidad)
            val normMun = normalize(loc.municipio)

            if (normLoc.startsWith(normQuery)) {
                exactOrPrefix.add(loc)
            } else if (normLoc.contains(normQuery)) {
                containsLoc.add(loc)
            } else if (normMun.contains(normQuery)) {
                containsMun.add(loc)
            }
        }

        val combined = (exactOrPrefix + containsLoc + containsMun).distinctBy { "${it.postalCode}_${it.localidad}" }
        return combined.take(limit)
    }

    fun getAllLocations(): List<CanaryPostalLocation> = locations
}
