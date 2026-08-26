package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.TimeZone

@Composable
fun SunAndUvBlock(
    uvIndex: Double?,
    sunrise: String?,
    sunset: String?,
    isDarkTheme: Boolean
) {
    val cardBg = if (isDarkTheme) Color(0xFF1E1C24) else Color.White
    val onSurface = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val labelColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF6B7280)
    val subCardBg = if (isDarkTheme) Color(0xFF282532) else Color(0xFFF4F7FA)

    // Current time calculations for sun position (Canary Islands Timezone)
    val canaryCalendar = remember {
        Calendar.getInstance(TimeZone.getTimeZone("Atlantic/Canary"))
    }
    val currentHour = canaryCalendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = canaryCalendar.get(Calendar.MINUTE)
    val currentTotalMinutes = currentHour * 60 + currentMinute
    val currentTimeString = String.format("%02d:%02d", currentHour, currentMinute)

    val sunriseMinutes = remember(sunrise) { parseTimeToMinutes(sunrise) ?: (7 * 60 + 30) }
    val sunsetMinutes = remember(sunset) { parseTimeToMinutes(sunset) ?: (20 * 60 + 30) }

    val totalDaylightMinutes = (sunsetMinutes - sunriseMinutes).coerceAtLeast(1)
    val daylightHours = totalDaylightMinutes / 60
    val daylightMins = totalDaylightMinutes % 60

    val solarNoonMinutes = (sunriseMinutes + sunsetMinutes) / 2
    val solarNoonString = String.format("%02d:%02d", solarNoonMinutes / 60, solarNoonMinutes % 60)

    val isDaytime = currentTotalMinutes in sunriseMinutes..sunsetMinutes
    val sunProgress = if (isDaytime) {
        ((currentTotalMinutes - sunriseMinutes).toFloat() / totalDaylightMinutes).coerceIn(0f, 1f)
    } else {
        0f
    }

    val daylightRemainingMinutes = (sunsetMinutes - currentTotalMinutes).coerceAtLeast(0)
    val remainingHours = daylightRemainingMinutes / 60
    val remainingMins = daylightRemainingMinutes % 60

    val uvValue = uvIndex ?: 0.0
    val uvAdvice = remember(uvValue) { getUvAdvice(uvValue) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ==========================================
        // 1. SOL & TRAYECTORIA SOLAR CARD
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBg,
                contentColor = onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
            border = BorderStroke(
                1.dp,
                if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
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
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "SOL Y HORAS DE LUZ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    }

                    // Status Badge (Día / Noche / Horas de luz)
                    Surface(
                        color = if (isDaytime) Color(0xFFFFB300).copy(alpha = 0.15f) else Color(0xFF5C6BC0).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isDaytime) Color(0xFFFFB300) else Color(0xFF5C6BC0),
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (isDaytime) "Luz solar: ${daylightHours}h ${daylightMins}m" else "Noche",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDaytime) Color(0xFFFFB300) else Color(0xFF7986CB)
                            )
                        }
                    }
                }

                // ==========================================
                // Canvas Solar Parabola Chart
                // ==========================================
                SolarParabolaChart(
                    isDaytime = isDaytime,
                    sunProgress = sunProgress,
                    currentTimeString = currentTimeString,
                    sunrise = sunrise ?: "07:30",
                    sunset = sunset ?: "20:30",
                    isDarkTheme = isDarkTheme
                )

                // ==========================================
                // Sunrise / Sunset & Solar Noon Metric Cards
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sunrise Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = subCardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Amanecer",
                                tint = Color(0xFFFBC02D),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sunrise ?: "--:--",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = onSurface
                            )
                            Text(
                                text = "Amanecer",
                                fontSize = 11.sp,
                                color = labelColor
                            )
                        }
                    }

                    // Solar Noon / Cénit Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = subCardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness7,
                                contentDescription = "Cénit Solar",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = solarNoonString,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = onSurface
                            )
                            Text(
                                text = "Cénit Solar",
                                fontSize = 11.sp,
                                color = labelColor
                            )
                        }
                    }

                    // Sunset Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = subCardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Atardecer",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sunset ?: "--:--",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = onSurface
                            )
                            Text(
                                text = "Atardecer",
                                fontSize = 11.sp,
                                color = labelColor
                            )
                        }
                    }
                }

                // Daylight info footer
                if (isDaytime) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (daylightRemainingMinutes > 0) "Quedan ${remainingHours}h ${remainingMins}m de sol hoy" else "El sol se está poniendo",
                            fontSize = 12.sp,
                            color = labelColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. ÍNDICE UV Y RECOMENDACIONES CARD
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBg,
                contentColor = onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
            border = BorderStroke(
                1.dp,
                if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
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
                            imageVector = Icons.Default.WbIncandescent,
                            contentDescription = null,
                            tint = uvAdvice.color,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ÍNDICE DE RADIACIÓN UV",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    }

                    // Level Badge
                    Surface(
                        color = uvAdvice.color.copy(alpha = if (isDarkTheme) 0.2f else 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, uvAdvice.color.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = uvAdvice.levelName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = uvAdvice.color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Big UV Value Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", uvValue),
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = onSurface
                        )
                        Text(
                            text = "de 12+",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor
                        )
                    }

                    Text(
                        text = when {
                            uvValue < 3 -> "Sin riesgo directo"
                            uvValue < 6 -> "Protección necesaria"
                            uvValue < 8 -> "Protección extra"
                            uvValue < 11 -> "Evitar sol directo"
                            else -> "Peligro extremo"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = uvAdvice.color
                    )
                }

                // Enhanced UV segmented progress bar
                EnhancedUvBar(
                    uvValue = uvValue,
                    isDarkTheme = isDarkTheme
                )

                // Recommendation advice box
                Surface(
                    color = uvAdvice.color.copy(alpha = if (isDarkTheme) 0.12f else 0.08f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, uvAdvice.color.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = uvAdvice.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Recomendación de Protección",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurface
                            )
                        }

                        Text(
                            text = uvAdvice.recommendation,
                            fontSize = 12.sp,
                            color = onSurface,
                            lineHeight = 17.sp
                        )

                        // Actionable Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uvAdvice.chips.forEach { chipText ->
                                Surface(
                                    color = if (isDarkTheme) Color(0xFF1E1C24) else Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = chipText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Solar Parabola Canvas Curve
// ==========================================
@Composable
fun SolarParabolaChart(
    isDaytime: Boolean,
    sunProgress: Float,
    currentTimeString: String,
    sunrise: String,
    sunset: String,
    isDarkTheme: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SunGlow")
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SunPulseAnim"
    )

    val textMeasurer = rememberTextMeasurer()
    val horizonColor = if (isDarkTheme) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.1f)
    val curveColor = Color(0xFFFFB300)
    val labelColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width == 0f || height == 0f) return@Canvas

            val horizonY = height - 26f
            val peakY = 22f
            val parabolaHeight = horizonY - peakY

            // 1. Draw horizon base line
            drawLine(
                color = horizonColor,
                start = Offset(0f, horizonY),
                end = Offset(width, horizonY),
                strokeWidth = 2f
            )

            // 2. Parabola Path
            val curveSteps = 60
            val curvePath = Path()
            val fillPath = Path()

            fillPath.moveTo(0f, horizonY)

            for (step in 0..curveSteps) {
                val fraction = step.toFloat() / curveSteps
                val x = fraction * width
                // Parabolic formula: y = horizonY - 4 * H * x/W * (1 - x/W)
                val y = horizonY - 4 * parabolaHeight * fraction * (1 - fraction)
                if (step == 0) {
                    curvePath.moveTo(x, y)
                } else {
                    curvePath.lineTo(x, y)
                }
                fillPath.lineTo(x, y)
            }
            fillPath.lineTo(width, horizonY)
            fillPath.close()

            val gradientAlpha = if (isDaytime) {
                if (isDarkTheme) 0.28f else 0.18f
            } else 0.03f

            // Draw daylight area soft gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFD54F).copy(alpha = gradientAlpha),
                        Color(0xFFFFD54F).copy(alpha = 0.01f)
                    ),
                    startY = peakY,
                    endY = horizonY
                )
            )

            // Draw dashed trajectory line
            drawPath(
                path = curvePath,
                color = curveColor.copy(alpha = if (isDaytime) 0.9f else 0.15f),
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Zenith point marker at top center
            val zenithX = width / 2
            val zenithY = peakY
            drawCircle(
                color = Color(0xFFFF9800).copy(alpha = if (isDaytime) 1f else 0.15f),
                radius = 4f,
                center = Offset(zenithX, zenithY)
            )

            // 3. Draw Current Sun Position if Daytime
            if (isDaytime) {
                val clampedProgress = sunProgress.coerceIn(0.01f, 0.99f)
                val sunX = clampedProgress * width
                val sunY = horizonY - 4 * parabolaHeight * clampedProgress * (1 - clampedProgress)

                // Vertical projection line
                drawLine(
                    color = curveColor.copy(alpha = 0.5f),
                    start = Offset(sunX, sunY),
                    end = Offset(sunX, horizonY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Sun Halo / Glow
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                    radius = 16f * sunPulse,
                    center = Offset(sunX, sunY)
                )
                // Outer gold ring
                drawCircle(
                    color = Color(0xFFFFB300),
                    radius = 9f,
                    center = Offset(sunX, sunY)
                )
                // Core bright center
                drawCircle(
                    color = Color.White,
                    radius = 5.5f,
                    center = Offset(sunX, sunY)
                )

                // Current time text tag near sun
                val timeLayout = textMeasurer.measure(
                    text = currentTimeString,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993)
                    )
                )
                val textX = (sunX - timeLayout.size.width / 2).coerceIn(4f, width - timeLayout.size.width - 4f)
                val textY = (sunY - timeLayout.size.height - 10f).coerceAtLeast(2f)

                drawText(
                    textLayoutResult = timeLayout,
                    topLeft = Offset(textX, textY)
                )
            }

            // 4. Labels at bottom: Sunrise (left) and Sunset (right)
            val riseLayout = textMeasurer.measure(
                text = "🌅 $sunrise",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Medium
                )
            )
            drawText(
                textLayoutResult = riseLayout,
                topLeft = Offset(4f, horizonY + 6f)
            )

            val setLayout = textMeasurer.measure(
                text = "$sunset 🌇",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Medium
                )
            )
            drawText(
                textLayoutResult = setLayout,
                topLeft = Offset(width - setLayout.size.width - 4f, horizonY + 6f)
            )
        }
    }
}

// ==========================================
// Enhanced UV Gauge / Scale Bar
// ==========================================
@Composable
fun EnhancedUvBar(
    uvValue: Double,
    isDarkTheme: Boolean
) {
    val maxUv = 12.0
    val normalizedUv = (uvValue / maxUv).toFloat().coerceIn(0f, 1f)

    val uvGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4CAF50), // 0-2: Bajo (Verde)
            Color(0xFFFBC02D), // 3-5: Moderado (Amarillo)
            Color(0xFFF57C00), // 6-7: Alto (Naranja)
            Color(0xFFD32F2F), // 8-10: Muy Alto (Rojo)
            Color(0xFF7B1FA2)  // 11+: Extremo (Violeta)
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background bar
                drawRoundRect(
                    brush = uvGradient,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(10.dp.toPx())
                )

                // Indicator needle/circle
                val indicatorX = (normalizedUv * size.width).coerceIn(10.dp.toPx(), size.width - 10.dp.toPx())

                // Outer circle
                drawCircle(
                    color = Color.White,
                    radius = size.height / 1.4f,
                    center = Offset(indicatorX, size.height / 2)
                )
                // Border for needle
                drawCircle(
                    color = Color.Black.copy(alpha = 0.4f),
                    radius = size.height / 1.4f,
                    center = Offset(indicatorX, size.height / 2),
                    style = Stroke(width = 2.5f)
                )
                // Inner center dot
                drawCircle(
                    color = if (isDarkTheme) Color(0xFF1E1C24) else Color(0xFF004993),
                    radius = 4f,
                    center = Offset(indicatorX, size.height / 2)
                )
            }
        }

        // Scale numeric ticks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 10.sp, color = if (isDarkTheme) Color.Gray else Color.DarkGray)
            Text("3", fontSize = 10.sp, color = if (isDarkTheme) Color.Gray else Color.DarkGray)
            Text("6", fontSize = 10.sp, color = if (isDarkTheme) Color.Gray else Color.DarkGray)
            Text("8", fontSize = 10.sp, color = if (isDarkTheme) Color.Gray else Color.DarkGray)
            Text("11+", fontSize = 10.sp, color = if (isDarkTheme) Color.Gray else Color.DarkGray)
        }
    }
}

// Backward-compatibility alias for other screens (e.g., Marine)
@Composable
fun UvGradientBar(
    uvValue: Double,
    isDarkTheme: Boolean = false
) {
    EnhancedUvBar(uvValue = uvValue, isDarkTheme = isDarkTheme)
}

// ==========================================
// Helpers & UV Recommendations Engine
// ==========================================
data class UvAdvice(
    val levelName: String,
    val color: Color,
    val recommendation: String,
    val chips: List<String>
)

fun getUvAdvice(uv: Double): UvAdvice {
    return when {
        uv < 3.0 -> UvAdvice(
            levelName = "Bajo (0 - 2)",
            color = Color(0xFF4CAF50),
            recommendation = "Riesgo mínimo. Puedes disfrutar del aire libre con seguridad. En Canarias se recomienda usar gafas de sol en días muy luminosos.",
            chips = listOf("🕶️ Gafas de sol", "🛡️ Sin riesgo")
        )
        uv < 6.0 -> UvAdvice(
            levelName = "Moderado (3 - 5)",
            color = Color(0xFFFBC02D),
            recommendation = "Se requiere protección solar. Aplica crema FPS 30+, usa sombrero y permanece a la sombra durante el mediodía si tienes piel sensible.",
            chips = listOf("🧴 Crema FPS 30+", "🧢 Sombrero", "🕶️ Gafas UV400")
        )
        uv < 8.0 -> UvAdvice(
            levelName = "Alto (6 - 7)",
            color = Color(0xFFF57C00),
            recommendation = "Riesgo alto de quemaduras solares. Aplica crema protectora FPS 50+ cada 2 horas, viste ropa ligera y busca sombra entre las 12:00 y las 16:00.",
            chips = listOf("🧴 FPS 50+", "⛱️ Buscar sombra", "👕 Ropa protectora")
        )
        uv < 11.0 -> UvAdvice(
            levelName = "Muy Alto (8 - 10)",
            color = Color(0xFFD32F2F),
            recommendation = "Riesgo muy severo. Se pueden producir quemaduras en menos de 20 minutos. Evita la exposición en horas centrales y protégete activamente.",
            chips = listOf("⚠️ Evitar 11:00-17:00", "🧴 FPS 50+ Muy Alta", "⛱️ Sombra obligatoria")
        )
        else -> UvAdvice(
            levelName = "Extremo (11+)",
            color = Color(0xFF7B1FA2),
            recommendation = "¡Alerta por radiación extrema! Peligro de quemaduras cutáneas graves en pocos minutos. Evita actividades directas al sol en horas centrales.",
            chips = listOf("🚨 Peligro extremo", "🚫 No exponerse al sol", "🧴 Protección total")
        )
    }
}

fun parseTimeToMinutes(timeStr: String?): Int? {
    if (timeStr.isNullOrBlank()) return null
    return try {
        val timePart = if (timeStr.contains("T")) {
            timeStr.substringAfter("T").take(5)
        } else {
            timeStr.trim().take(5)
        }
        val parts = timePart.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        hours * 60 + minutes
    } catch (e: Exception) {
        null
    }
}
