package com.example.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(entities = [BeachEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun beachDao(): BeachDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beaches_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(BeachDatabaseCallback(context, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BeachDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.beachDao(), context)
                }
            }
        }

        private suspend fun populateDatabase(beachDao: BeachDao, context: Context) {
            val beachesList = mutableListOf<BeachEntity>()
            try {
                context.assets.open("playas.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val firstLine = reader.readLine() // Read header
                        val headers = firstLine?.split(";")?.map { it.trim() } ?: emptyList()
                        val clasificacionIdx = headers.indexOfFirst { it.equals("clasificación", ignoreCase = true) || it.equals("clasificacion", ignoreCase = true) }.takeIf { it >= 0 } ?: 5
                        val peligrosIdx = headers.indexOfFirst { it.equals("peligros", ignoreCase = true) }.takeIf { it >= 0 } ?: -1

                        var line: String? = reader.readLine()
                        while (line != null) {
                            if (line.isNotBlank()) {
                                val tokens = line.split(";")
                                if (tokens.size > 38) {
                                    try {
                                        val province = tokens.getOrNull(0)?.trim() ?: ""
                                        val island = tokens.getOrNull(1)?.trim() ?: ""
                                        val municipality = tokens.getOrNull(2)?.trim() ?: ""
                                        val name = tokens.getOrNull(3)?.trim() ?: ""
                                        val id = tokens.getOrNull(4)?.trim() ?: ""
                                        
                                        val clasificacion = tokens.getOrNull(clasificacionIdx)?.trim() ?: "Libre"
                                        val peligros = if (peligrosIdx >= 0) tokens.getOrNull(peligrosIdx)?.trim() ?: "" else ""

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
                                        val color = tokens.getOrNull(32)?.trim() ?: ""
                                        val condicionesBano = tokens.getOrNull(33)?.trim() ?: ""
                                        val condicionesEntorno = tokens.getOrNull(34)?.trim() ?: ""
                                        val condicionesAcceso = tokens.getOrNull(35)?.trim() ?: ""
                                        
                                        val latStr = tokens.getOrNull(37)?.trim() ?: ""
                                        val lngStr = tokens.getOrNull(38)?.trim() ?: ""
                                        val lat = parseDms(latStr) ?: 0.0
                                        val lng = parseDms(lngStr) ?: 0.0

                                        if (id.isNotEmpty() && name.isNotEmpty()) {
                                            beachesList.add(
                                                BeachEntity(
                                                    id = id,
                                                    nombre = name,
                                                    provincia = province,
                                                    isla = island,
                                                    municipio = municipality,
                                                    lat = lat,
                                                    lng = lng,
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
                                                    condicionesAcceso = condicionesAcceso,
                                                    clasificacion = clasificacion,
                                                    peligros = peligros
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AppDatabase", "Error parsing line: $line", e)
                                    }
                                }
                            }
                            line = reader.readLine()
                        }
                    }
                }
                
                if (beachesList.isNotEmpty()) {
                    // Remove duplicates by ID just in case
                    val distinctBeaches = beachesList.distinctBy { it.id }
                    beachDao.insertAll(distinctBeaches)
                }
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error populating database", e)
            }
        }

        private fun parseDms(dmsStr: String): Double? {
            if (dmsStr.isBlank()) return null
            try {
                val trimmed = dmsStr.trim()
                trimmed.toDoubleOrNull()?.let { return it }

                var clean = trimmed.replace("\"", "").trim()
                val isNegative = clean.contains("W", ignoreCase = true) || clean.contains("S", ignoreCase = true)
                
                clean = clean.replace("[NWSEnwse]".toRegex(), "").trim()
                clean = clean.replace("°", " ")
                             .replace("º", " ")
                             .replace("'", " ")
                             .replace(",", ".")
                
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
                return null
            }
        }
    }
}
