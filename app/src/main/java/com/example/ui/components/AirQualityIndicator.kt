package com.example.ui.components

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AirQualityData
import com.example.data.CalimaSeverity

@Composable
fun AirQualityIndicator(
    airQuality: AirQualityData?,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (airQuality == null) return

    val isCalimaAlertActive = airQuality.calimaSeverity == CalimaSeverity.MODERATE ||
            airQuality.calimaSeverity == CalimaSeverity.SEVERE

    // Pulsing animation for the warning banner if Calima is detected
    val infiniteTransition = rememberInfiniteTransition(label = "CalimaPulsing")
    val pulseBeta by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseBetaAnim"
    )

    val cardBg = if (isDarkTheme) Color(0xFF1E1C24) else Color.White
    val onSurface = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val titleColor = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993)
    val subCardBg = if (isDarkTheme) Color(0xFF282532) else Color(0xFFF4F7FA)
    val labelColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF6B7280)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = onSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "CALIDAD DEL AIRE (ICA / AQI)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface
                )
            }

            // Canary Islands AQI Card (Official Scale)
            val aqiLevel = airQuality.canaryAqiLevel
            val levelColor = Color(aqiLevel.color)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = levelColor.copy(alpha = if (isDarkTheme) 0.18f else 0.12f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, levelColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Índice de Calidad del Aire", fontSize = 12.sp, color = labelColor, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aqiLevel.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Recomendación general:",
                        fontSize = 11.sp,
                        color = labelColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = aqiLevel.generalRecommendation,
                        fontSize = 12.sp,
                        color = onSurface,
                        modifier = Modifier.align(Alignment.Start),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grupos de riesgo:",
                        fontSize = 11.sp,
                        color = labelColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = aqiLevel.sensitiveRecommendation,
                        fontSize = 12.sp,
                        color = onSurface,
                        modifier = Modifier.align(Alignment.Start),
                        lineHeight = 16.sp
                    )
                }
            }

            // Calima visual warning banner
            if (isCalimaAlertActive) {
                val calimaColor = if (airQuality.calimaSeverity == CalimaSeverity.SEVERE) {
                    Color(0xFFD32F2F) // Severe: Red
                } else {
                    Color(0xFFE65100) // Moderate: Deep Amber
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(calimaColor.copy(alpha = 0.12f * pulseBeta))
                        .border(1.5.dp, calimaColor.copy(alpha = pulseBeta), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta Calima",
                            tint = calimaColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "CALIMA DETECTADA (${if (airQuality.calimaSeverity == CalimaSeverity.SEVERE) "ALTA" else "MODERADA"})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = calimaColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = airQuality.calimaAlertMessage ?: "",
                                fontSize = 12.sp,
                                color = onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Pollution metrics list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricRow(name = "Partículas Suspendidas (PM10)", value = airQuality.pm10, maxVal = 150.0, unit = "µg/m³", isCalimaInd = true, euLimit = 50.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Partículas Finas (PM2.5)", value = airQuality.pm25, maxVal = 75.0, unit = "µg/m³", euLimit = 25.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Dióxido de Nitrógeno (NO₂)", value = airQuality.no2, maxVal = 340.0, unit = "µg/m³", euLimit = 120.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Ozono (O₃)", value = airQuality.o3, maxVal = 380.0, unit = "µg/m³", euLimit = 130.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Dióxido de Azufre (SO₂)", value = airQuality.so2, maxVal = 750.0, unit = "µg/m³", euLimit = 350.0, isDarkTheme = isDarkTheme)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.width(2.dp).height(10.dp).background(onSurface))
                Text(
                    text = "El marcador vertical indica el umbral del nivel 'Desfavorable'",
                    fontSize = 11.sp,
                    color = labelColor,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun MetricRow(
    name: String,
    value: Double,
    maxVal: Double,
    unit: String,
    isCalimaInd: Boolean = false,
    euLimit: Double? = null,
    isDarkTheme: Boolean = false
) {
    val progress = (value / maxVal).coerceIn(0.0, 1.0).toFloat()
    val onSurface = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)

    // Determine bar color based on toxicity levels
    val barColor = when {
        euLimit != null && value > euLimit -> Color(0xFFD32F2F)
        progress > 0.45f -> Color(0xFFFBBF24)
        else -> Color(0xFF10B981)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = onSurface)
                if (isCalimaInd && value > 50.0) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE65100), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text("Calima", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "${String.format("%.1f", value)} $unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        // Horizontal progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .background(barColor)
            )

            if (euLimit != null) {
                val limitFraction = (euLimit / maxVal).coerceIn(0.001, 1.0).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = limitFraction)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(onSurface)
                    )
                }
            }
        }
    }
}

// Helpers removed

@Composable
fun CompactAirQualitySummary(
    airQuality: AirQualityData?,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (airQuality == null) return

    val cardBg = if (isDarkTheme) Color(0xFF1E1C24) else Color.White
    val onSurface = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val labelColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF6B7280)

    val aqiLevel = airQuality.canaryAqiLevel
    val levelColor = Color(aqiLevel.color)
    val isCalimaActive = airQuality.calimaSeverity == CalimaSeverity.MODERATE || airQuality.calimaSeverity == CalimaSeverity.SEVERE

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg,
            contentColor = onSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = levelColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CALIDAD DEL AIRE (ICA)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                }

                // Level Badge
                Surface(
                    color = levelColor.copy(alpha = if (isDarkTheme) 0.2f else 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, levelColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = aqiLevel.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Calima Alert Badge if active
            if (isCalimaActive) {
                val calimaColor = if (airQuality.calimaSeverity == CalimaSeverity.SEVERE) Color(0xFFD32F2F) else Color(0xFFE65100)
                Surface(
                    color = calimaColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, calimaColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = calimaColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Calima detectada: ${airQuality.calimaAlertMessage ?: "Niveles elevados de polvo sahariano"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurface
                        )
                    }
                }
            }

            // Key Metrics in 3 neat columns (PM10, PM2.5, NO2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AirMiniMetric(
                    label = "PM10 (Polvo)",
                    value = "${airQuality.pm10.toInt()} µg/m³",
                    isWarning = airQuality.pm10 > 50.0,
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme
                )
                AirMiniMetric(
                    label = "PM2.5 (Finas)",
                    value = "${airQuality.pm25.toInt()} µg/m³",
                    isWarning = airQuality.pm25 > 25.0,
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme
                )
                AirMiniMetric(
                    label = "NO₂ (Gases)",
                    value = "${airQuality.no2.toInt()} µg/m³",
                    isWarning = airQuality.no2 > 120.0,
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme
                )
            }

            // Recommendation text
            Text(
                text = aqiLevel.generalRecommendation,
                fontSize = 12.sp,
                color = labelColor,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AirMiniMetric(
    label: String,
    value: String,
    isWarning: Boolean,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val subCardBg = if (isDarkTheme) Color(0xFF282532) else Color(0xFFF4F7FA)
    val onSurface = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val labelColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF6B7280)

    Surface(
        modifier = modifier,
        color = subCardBg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = labelColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) Color(0xFFE65100) else onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

