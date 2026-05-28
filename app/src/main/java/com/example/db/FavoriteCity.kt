package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "favorite_cities")
data class FavoriteCity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isPredefined: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) : Serializable
