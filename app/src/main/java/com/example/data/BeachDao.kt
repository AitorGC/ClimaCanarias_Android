package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class BeachPartial(
    val id: String,
    val nombre: String,
    val provincia: String,
    val isla: String,
    val municipio: String
)

@Dao
interface BeachDao {
    @Query("SELECT id, nombre, provincia, isla, municipio FROM beaches ORDER BY nombre ASC")
    fun getPartialBeaches(): Flow<List<BeachPartial>>

    @Query("SELECT * FROM beaches WHERE id = :id")
    suspend fun getBeachDetailsById(id: String): BeachEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(beaches: List<BeachEntity>)
}
