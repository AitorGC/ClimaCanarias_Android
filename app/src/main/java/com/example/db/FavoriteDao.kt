package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(city: FavoriteCity)

    @Delete
    suspend fun deleteFavorite(city: FavoriteCity)

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int)

    @Query("DELETE FROM favorite_cities")
    suspend fun deleteAllFavorites()

    @Query("SELECT * FROM favorite_cities")
    suspend fun getFavoritesListSync(): List<FavoriteCity>

    @Query("SELECT COUNT(*) FROM favorite_cities")
    suspend fun getCount(): Int
}
