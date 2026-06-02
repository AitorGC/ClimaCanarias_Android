package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_beaches")
data class FavoriteBeach(
    @PrimaryKey val id: String,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)
