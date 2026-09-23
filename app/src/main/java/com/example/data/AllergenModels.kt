package com.example.data

import com.example.repository.AllergySettings

/**
 * Aerobiological Alert Levels following standard REA (Red Española de Aerobiología)
 * and Canary Islands Aerobiology & Air Quality surveillance guidelines.
 */
enum class AllergenSeverity(val label: String, val levelPriority: Int) {
    MODERATE("Moderado", 1),
    HIGH("Alto", 2),
    VERY_HIGH("Muy Alto", 3)
}

enum class AllergenType(
    val id: String,
    val displayName: String,
    val scientificFamily: String,
    val iconName: String
) {
    GRASS("grass", "Gramíneas", "Poaceae", "grass"),
    OLIVE("olive", "Olivo", "Oleaceae", "park"),
    MUGWORT("mugwort", "Artemisa / Maleza", "Asteraceae", "nature"),
    ALDER("alder", "Aliso", "Betulaceae / Alnus", "forest"),
    BIRCH("birch", "Abedul", "Betula", "eco"),
    RAGWEED("ragweed", "Ambrosía", "Ambrosia", "grain"),
    DUST_CALIMA("dust", "Polvo Sahariano / Calima", "Partículas PM10 y PM2.5", "air")
}

data class AllergenAlertItem(
    val allergenType: AllergenType,
    val severity: AllergenSeverity,
    val currentConcentration: Double,
    val unit: String,
    val title: String,
    val alertDescription: String,
    val recommendations: List<String>
)

object AllergenEvaluator {

    /**
     * Official thresholds for aeroallergens in grains/m³ based on REA guidelines:
     * - Gramíneas (Poaceae): Moderate > 25, High > 50
     * - Olivo (Olea): Moderate > 50, High > 200
     * - Artemisa (Artemisia / Mugwort): Moderate > 20, High > 50
     * - Aliso (Alnus): Moderate > 30, High > 75
     * - Abedul (Betula): Moderate > 30, High > 75
     * - Ambrosía (Ambrosia): Moderate > 15, High > 40
     * - Calima/Dust (PM10 en µg/m³): Moderate > 45, High > 75, Severe > 120
     */
    fun evaluateAlerts(
        airQuality: AirQualityData?,
        settings: AllergySettings
    ): List<AllergenAlertItem> {
        if (airQuality == null) return emptyList()

        val alerts = mutableListOf<AllergenAlertItem>()

        // 1. Gramíneas
        if (settings.allergyGrass) {
            val v = airQuality.grassPollen
            val severity = when {
                v >= 100.0 -> AllergenSeverity.VERY_HIGH
                v >= 50.0 -> AllergenSeverity.HIGH
                v >= 25.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.GRASS,
                        severity = severity,
                        currentConcentration = v,
                        unit = "granos/m³",
                        title = "Polen de Gramíneas: ${severity.label}",
                        alertDescription = "Concentración de $v granos/m³ detectada en el aire.",
                        recommendations = listOf(
                            "Evita actividades intensas al aire libre a primera y última hora.",
                            "Mantén cerradas las ventanas durante las horas centrales y el atardecer.",
                            "Usa gafas de sol para reducir el contacto ocular con el polen.",
                            "Lávate la cara y cámbiate de ropa tras llegar de la calle."
                        )
                    )
                )
            }
        }

        // 2. Olivo
        if (settings.allergyOlive) {
            val v = airQuality.olivePollen
            val severity = when {
                v >= 250.0 -> AllergenSeverity.VERY_HIGH
                v >= 150.0 -> AllergenSeverity.HIGH
                v >= 40.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.OLIVE,
                        severity = severity,
                        currentConcentration = v,
                        unit = "granos/m³",
                        title = "Polen de Olivo: ${severity.label}",
                        alertDescription = "Concentración de $v granos/m³ en la atmósfera local.",
                        recommendations = listOf(
                            "Vigila la aparición de conjuntivitis o rinitis alérgica.",
                            "Utiliza filtros antipolen en el vehículo y ventila con precaución.",
                            "Ten a mano medicación pautada (antihistamínicos o sprays nasales)."
                        )
                    )
                )
            }
        }

        // 3. Artemisa / Maleza
        if (settings.allergyMugwort) {
            val v = airQuality.mugwortPollen
            val severity = when {
                v >= 80.0 -> AllergenSeverity.VERY_HIGH
                v >= 40.0 -> AllergenSeverity.HIGH
                v >= 15.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.MUGWORT,
                        severity = severity,
                        currentConcentration = v,
                        unit = "granos/m³",
                        title = "Polen de Artemisa / Maleza: ${severity.label}",
                        alertDescription = "Nivel de maleza en $v granos/m³.",
                        recommendations = listOf(
                            "Evita paseos por zonas de campo abierto o descampados no urbanizados.",
                            "Seca la ropa en el interior de la vivienda para evitar adherencia de polen."
                        )
                    )
                )
            }
        }

        // 4. Aliso
        if (settings.allergyAlder) {
            val v = airQuality.alderPollen
            val severity = when {
                v >= 100.0 -> AllergenSeverity.VERY_HIGH
                v >= 60.0 -> AllergenSeverity.HIGH
                v >= 25.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.ALDER,
                        severity = severity,
                        currentConcentration = v,
                        unit = "granos/m³",
                        title = "Polen de Aliso: ${severity.label}",
                        alertDescription = "Nivel de polen de aliso en $v granos/m³.",
                        recommendations = listOf(
                            "Precaución en zonas de arboledas húmedas o barrancos arbolados.",
                            "Uso recomendado de gafas protectoras en exteriores."
                        )
                    )
                )
            }
        }

        // 5. Abedul
        if (settings.allergyBirch) {
            val v = airQuality.birchPollen
            val severity = when {
                v >= 100.0 -> AllergenSeverity.VERY_HIGH
                v >= 50.0 -> AllergenSeverity.HIGH
                v >= 25.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.BIRCH,
                        severity = severity,
                        currentConcentration = v,
                        unit = "granos/m³",
                        title = "Polen de Abedul: ${severity.label}",
                        alertDescription = "Concentración de abedul en $v granos/m³.",
                        recommendations = listOf(
                            "Alérgeno muy reactivo en personas con asma alérgica estacional.",
                            "Evita la ventilación prolongada de habitaciones a primera hora del día."
                        )
                    )
                )
            }
        }

        // 6. Ambrosía
        if (settings.allergyRagweed) {
            val v = airQuality.ragweedPollen
            val severity = when {
                v >= 60.0 -> AllergenSeverity.VERY_HIGH
                v >= 30.0 -> AllergenSeverity.HIGH
                v >= 12.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.RAGWEED,
                        severity = severity,
                        currentConcentration = v,
                        unit = "granos/m³",
                        title = "Polen de Ambrosía: ${severity.label}",
                        alertDescription = "Concentración de ambrosía en $v granos/m³.",
                        recommendations = listOf(
                            "Gran capacidad irritante de las vías respiratorias superiores.",
                            "Usa mascarilla homologada si realizas desplazamientos a pie por zonas expuestas."
                        )
                    )
                )
            }
        }

        // 7. Calima / Polvo Sahariano
        if (settings.sensitiveToDust) {
            val pm10 = airQuality.pm10
            val calima = airQuality.calimaSeverity
            val severity = when {
                calima == CalimaSeverity.SEVERE || pm10 >= 150.0 -> AllergenSeverity.VERY_HIGH
                calima == CalimaSeverity.MODERATE || pm10 >= 75.0 -> AllergenSeverity.HIGH
                calima == CalimaSeverity.LOW || pm10 >= 45.0 -> AllergenSeverity.MODERATE
                else -> null
            }
            if (severity != null) {
                alerts.add(
                    AllergenAlertItem(
                        allergenType = AllergenType.DUST_CALIMA,
                        severity = severity,
                        currentConcentration = pm10,
                        unit = "µg/m³ (PM10)",
                        title = "Polvo en Suspensión / Calima: ${severity.label}",
                        alertDescription = "Concentración de partículas PM10 en ${pm10.toInt()} µg/m³. Afectación directa a vías respiratorias.",
                        recommendations = listOf(
                            "Cierra puertas y ventanas; limpia superficies con paños húmedos.",
                            "Usa mascarilla FFP2 en exteriores si perteneces a grupos de riesgo.",
                            "Bebe abundantes líquidos y mantén la medicación de rescate si padeces asma o EPOC.",
                            "Evita ejercicios físicos intensos en la calle mientras dure el episodio."
                        )
                    )
                )
            }
        }

        // Sort descending by priority (VERY_HIGH first)
        return alerts.sortedByDescending { it.severity.levelPriority }
    }
}
