package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Text(
                text = "Índice de Calidad del Aire (ICA/AQI)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // European and American index tiles (Bento sub-grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // European AQI Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = getEuAqiColor(airQuality.europeanAqi).copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "AQI Europeo", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getEuAqiLabel(airQuality.europeanAqi),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getEuAqiColor(airQuality.europeanAqi)
                        )
                    }
                }

                // American AQI Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = getUsAqiColor(airQuality.americanAqi).copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "AQI Americano", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${airQuality.americanAqi} (${getUsAqiLabel(airQuality.americanAqi)})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = getUsAqiColor(airQuality.americanAqi)
                        )
                    }
                }
            }

            // Calima visual warning banner if there is high/moderate calima
            if (isCalimaAlertActive) {
                val calimaColor = if (airQuality.calimaSeverity == CalimaSeverity.SEVERE) {
                    Color(0xFFD32F2F) // Severe: Red
                } else {
                    Color(0xFFE65100) // Moderate: Solid Deep Amber
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(calimaColor.copy(alpha = 0.1f * pulseBeta))
                        .border(1.5.dp, calimaColor.copy(alpha = pulseBeta), RoundedCornerShape(12.dp))
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
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Pollution metrics lists
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricRow(name = "Partículas Suspendidas (PM10)", value = airQuality.pm10, maxVal = 180.0, unit = "µg/m³", isCalimaInd = true)
                MetricRow(name = "Partículas Finas (PM2.5)", value = airQuality.pm25, maxVal = 80.0, unit = "µg/m³")
                MetricRow(name = "Dióxido de Nitrógeno (NO₂)", value = airQuality.no2, maxVal = 120.0, unit = "µg/m³")
                MetricRow(name = "Ozono (O₃)", value = airQuality.o3, maxVal = 150.0, unit = "µg/m³")
                MetricRow(name = "Monóxido de Carbono (CO)", value = airQuality.co, maxVal = 5.0, unit = "mg/m³")
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
    isCalimaInd: Boolean = false
) {
    val progress = (value / maxVal).coerceIn(0.0, 1.0).toFloat()
    
    // Determine bar color based on toxicity levels
    val barColor = when {
        progress > 0.75f -> Color(0xFFD32F2F) // Dangerous warning
        progress > 0.45f -> Color(0xFFFBBF24) // Warning
        else -> Color(0xFF10B981) // Safe
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
                Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .background(barColor)
            )
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
        index <= 150 -> "Insalubre (Grupos de riesgo)"
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
