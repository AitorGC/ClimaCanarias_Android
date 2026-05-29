package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SunAndUvBlock(
    uvIndex: Double?,
    sunrise: String?,
    sunset: String?,
    isDarkTheme: Boolean
) {
    val cardBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val onSurface = if (isDarkTheme) Color.White else Color(0xFF2C3E50)
    val dividerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
        border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Sunrise and Sunset Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sunrise
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Amanecer",
                        modifier = Modifier.size(36.dp),
                        tint = Color(0xFFFBC02D)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = sunrise ?: "--:--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = onSurface
                        )
                        Text(
                            text = "Amanecer",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.LightGray else Color.Gray
                        )
                    }
                }

                Divider(
                    modifier = Modifier.height(40.dp).width(1.dp),
                    color = dividerColor
                )

                // Sunset
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.NightsStay,
                        contentDescription = "Atardecer",
                        modifier = Modifier.size(36.dp),
                        tint = Color(0xFFFFA000)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = sunset ?: "--:--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = onSurface
                        )
                        Text(
                            text = "Atardecer",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.LightGray else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // UV Index Scale
            Text(
                text = "Índice UV",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            val uvValue = uvIndex ?: 0.0
            val uvLabel = when {
                uvValue < 3 -> "Bajo"
                uvValue < 6 -> "Moderado"
                uvValue < 8 -> "Alto"
                uvValue < 11 -> "Muy Alto"
                else -> "Extremo"
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.1f", uvValue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = onSurface
                )
                Text(
                    text = uvLabel,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = if (isDarkTheme) Color.LightGray else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Draw the UV Gradient bar
            UvGradientBar(uvValue = uvValue)
        }
    }
}

@Composable
fun UvGradientBar(uvValue: Double) {
    // UV scale usually goes from 0 to ~12+
    val maxUv = 12.0
    val normalizedUv = (uvValue / maxUv).toFloat().coerceIn(0f, 1f)
    
    // Low(Green), Moderate(Yellow), High(Orange), Very High(Red), Extreme(Purple)
    val uvGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF8BC34A), // 0-2: Low
            Color(0xFFFFEB3B), // 3-5: Moderate
            Color(0xFFFF9800), // 6-7: High
            Color(0xFFF44336), // 8-10: Very High
            Color(0xFF9C27B0)  // 11+: Extreme
        )
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
        drawRoundRect(
            brush = uvGradient,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        
        // Draw the indicator circle at normalizedUv position
        val indicatorX = normalizedUv * size.width
        drawCircle(
            color = Color.White,
            radius = size.height / 1.5f,
            center = Offset(indicatorX, size.height / 2)
        )
        
        // Stroke for the indicator
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = size.height / 1.5f,
            center = Offset(indicatorX, size.height / 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}
