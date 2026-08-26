package com.example.ui.components

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

            // European and American index tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val euColor = getEuAqiColor(airQuality.europeanAqi)
                val usColor = getUsAqiColor(airQuality.americanAqi)

                // European AQI Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = euColor.copy(alpha = if (isDarkTheme) 0.18f else 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, euColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "AQI Europeo", fontSize = 11.sp, color = labelColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getEuAqiLabel(airQuality.europeanAqi),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = euColor
                        )
                    }
                }

                // American AQI Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = usColor.copy(alpha = if (isDarkTheme) 0.18f else 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, usColor.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "AQI Americano", fontSize = 11.sp, color = labelColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${airQuality.americanAqi} (${getUsAqiLabel(airQuality.americanAqi)})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = usColor
                        )
                    }
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
                MetricRow(name = "Partículas Suspendidas (PM10)", value = airQuality.pm10, maxVal = 180.0, unit = "µg/m³", isCalimaInd = true, euLimit = 50.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Partículas Finas (PM2.5)", value = airQuality.pm25, maxVal = 80.0, unit = "µg/m³", euLimit = 25.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Dióxido de Nitrógeno (NO₂)", value = airQuality.no2, maxVal = 120.0, unit = "µg/m³", euLimit = 40.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Ozono (O₃)", value = airQuality.o3, maxVal = 150.0, unit = "µg/m³", euLimit = 120.0, isDarkTheme = isDarkTheme)
                MetricRow(name = "Monóxido de Carbono (CO)", value = airQuality.co, maxVal = 15.0, unit = "mg/m³", euLimit = 10.0, isDarkTheme = isDarkTheme)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.width(2.dp).height(10.dp).background(onSurface))
                Text(
                    text = "El marcador vertical indica el límite máximo recomendado por la UE",
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

// Helpers
fun getEuAqiLabel(index: Int): String {
    return when (index) {
        1 -> "Excelente (1)"
        2 -> "Bueno (2)"
        3 -> "Moderado (3)"
        4 -> "Pobre (4)"
        5 -> "Muy Pobre (5)"
        else -> "Desconocido"
    }
}

fun getEuAqiColor(index: Int): Color {
    return when (index) {
        1 -> Color(0xFF10B981)
        2 -> Color(0xFF34D399)
        3 -> Color(0xFFFBBF24)
        4 -> Color(0xFFF97316)
        5 -> Color(0xFFEF4444)
        else -> Color.Gray
    }
}

fun getUsAqiLabel(index: Int): String {
    return when {
        index <= 50 -> "Excelente"
        index <= 100 -> "Moderado"
        index <= 150 -> "Insalubre (Riesgo)"
        else -> "Insalubre"
    }
}

fun getUsAqiColor(index: Int): Color {
    return when {
        index <= 50 -> Color(0xFF10B981)
        index <= 100 -> Color(0xFFFBBF24)
        index <= 150 -> Color(0xFFF97316)
        else -> Color(0xFFEF4444)
    }
}
