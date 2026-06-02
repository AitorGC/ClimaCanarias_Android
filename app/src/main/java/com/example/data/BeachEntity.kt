package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beaches")
data class BeachEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val provincia: String,
    val isla: String,
    val municipio: String,
    val lat: Double,
    val lng: Double,
    val riesgo: String,
    val banderaAzul: Boolean,
    val accesoPmr: Boolean,
    val duchas: Boolean,
    val aparcar: Boolean,
    val tipoArena: String,
    val condicionesBano: String,
    val duchaAdaptada: Boolean,
    val aseoAdaptado: Boolean,
    val banoAsistido: Boolean,
    val sombraPmr: Boolean,
    val aseos: Boolean,
    val lavapies: Boolean,
    val alquilerSombrillas: Boolean,
    val alquilerHamacas: Boolean,
    val alquilerNautico: Boolean,
    val areaInfantil: Boolean,
    val areaDeportiva: Boolean,
    val color: String,
    val condicionesEntorno: String,
    val condicionesAcceso: String,
    val clasificacion: String,
    val peligros: String
)
