package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HourlyForecastItem

@Composable
fun TrendChart(
    hourlyItems: List<HourlyForecastItem>,
    isCelsius: Boolean,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (hourlyItems.isEmpty()) return

    // Limit to 12-13 items to cover the next 12 hours
    val chartItems = remember(hourlyItems) {
        hourlyItems.take(13)
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    val cardBg = if (isDarkTheme) Color(0xFF1E1C24) else Color.White
    val onSurface = if (isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val titleColor = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993)
    val gridColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val labelColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color(0xFF6B7280)
    val tooltipBg = if (isDarkTheme) Color(0xFF282532) else Color(0xFFF0F4F9)

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
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
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
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = titleColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PRÓXIMAS 12 HORAS",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )
                    )
                }

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFFFB300), label = "Temp", textColor = labelColor)
                    LegendItem(color = Color(0xFF26A69A), label = "Hum", textColor = labelColor)
                    LegendItem(color = Color(0xFF29B6F6), label = "% Lluvia", textColor = labelColor)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tooltip preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedIndex != null && selectedIndex!! < chartItems.size) {
                    val item = chartItems[selectedIndex!!]
                    val displayTemp = if (isCelsius) {
                        "${item.temperature}°C"
                    } else {
                        "${String.format("%.1f", item.temperature * 9 / 5 + 32)}°F"
                    }
                    Text(
                        text = "${item.timeString} ➔ Temp: $displayTemp | Hum: ${item.humidity.toInt()}% | Lluvia: ${item.precipitationProbability}%",
                        color = if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                color = tooltipBg,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                } else {
                    Text(
                        text = "Toca o desliza en el gráfico para ver el detalle de cada hora",
                        color = labelColor,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Native interactive Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(chartItems) {
                            detectTapGestures { offset ->
                                val xStep = size.width / (chartItems.size - 1).coerceAtLeast(1)
                                val index = (offset.x / xStep).toInt().coerceIn(0, chartItems.size - 1)
                                selectedIndex = index
                            }
                        }
                        .pointerInput(chartItems) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val xStep = size.width / (chartItems.size - 1).coerceAtLeast(1)
                                    val index = (offset.x / xStep).toInt().coerceIn(0, chartItems.size - 1)
                                    selectedIndex = index
                                },
                                onDragEnd = { selectedIndex = null },
                                onDragCancel = { selectedIndex = null },
                                onDrag = { change, _ ->
                                    val xStep = size.width / (chartItems.size - 1).coerceAtLeast(1)
                                    val index = (change.position.x / xStep).toInt().coerceIn(0, chartItems.size - 1)
                                    selectedIndex = index
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    if (width == 0f || height == 0f || chartItems.isEmpty()) return@Canvas

                    val paddingBottom = 26f
                    val paddingTop = 18f
                    val graphHeight = height - paddingBottom - paddingTop

                    val temps = chartItems.map { it.temperature }
                    val minTemp = (temps.minOrNull() ?: 15.0) - 1.0
                    val maxTemp = (temps.maxOrNull() ?: 35.0) + 1.0
                    val tempRange = if (maxTemp == minTemp) 1.0 else maxTemp - minTemp

                    val stepX = width / (chartItems.size - 1).coerceAtLeast(1)

                    // 1. Grid background lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val gridY = paddingTop + (graphHeight * i / gridLines)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Precipitation probability as rounded bars
                    chartItems.forEachIndexed { i, item ->
                        val barWidth = (stepX * 0.35f).coerceAtLeast(6f)
                        val barHeight = graphHeight * (item.precipitationProbability / 100f)
                        val barX = i * stepX - barWidth / 2
                        val barY = paddingTop + graphHeight - barHeight

                        if (barHeight > 2f) {
                            drawRoundRect(
                                color = Color(0xFF29B6F6).copy(alpha = if (isDarkTheme) 0.35f else 0.25f),
                                topLeft = Offset(barX, barY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }

                    // 3. Humidity line
                    val humidityPoints = chartItems.mapIndexed { i, item ->
                        val ratio = item.humidity / 100f
                        val y = paddingTop + graphHeight - (graphHeight * ratio).toFloat()
                        Offset(i * stepX, y)
                    }

                    val humidityPath = Path().apply {
                        humidityPoints.forEachIndexed { i, point ->
                            if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = humidityPath,
                        color = Color(0xFF26A69A).copy(alpha = if (isDarkTheme) 0.7f else 0.85f),
                        style = Stroke(
                            width = 2.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    )

                    // 4. Temperature bezier curve
                    val tempPoints = chartItems.mapIndexed { i, item ->
                        val norm = ((item.temperature - minTemp) / tempRange).coerceIn(0.0, 1.0)
                        val y = paddingTop + graphHeight - (graphHeight * norm).toFloat()
                        Offset(i * stepX, y)
                    }

                    val tempPath = Path().apply {
                        if (tempPoints.isNotEmpty()) {
                            moveTo(tempPoints[0].x, tempPoints[0].y)
                            for (i in 0 until tempPoints.size - 1) {
                                val p0 = tempPoints[i]
                                val p1 = tempPoints[i + 1]
                                val controlX = (p0.x + p1.x) / 2
                                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                        }
                    }

                    // Gradient fill below temperature curve
                    val fillPath = Path().apply {
                        addPath(tempPath)
                        lineTo(width, paddingTop + graphHeight)
                        lineTo(0f, paddingTop + graphHeight)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFB300).copy(alpha = if (isDarkTheme) 0.35f else 0.22f),
                                Color(0xFFFFB300).copy(alpha = 0.0f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + graphHeight
                        )
                    )

                    // Main temperature curve line
                    drawPath(
                        path = tempPath,
                        color = Color(0xFFFFB300),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // 5. Dots and labels
                    chartItems.forEachIndexed { i, item ->
                        val x = i * stepX
                        val point = tempPoints[i]

                        // Selection indicator
                        if (selectedIndex == i) {
                            drawLine(
                                color = (if (isDarkTheme) Color(0xFFFFD600) else Color(0xFF004993)).copy(alpha = 0.6f),
                                start = Offset(x, paddingTop),
                                end = Offset(x, paddingTop + graphHeight),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            drawCircle(
                                color = Color(0xFFFFB300),
                                radius = 12f,
                                center = point
                            )
                            drawCircle(
                                color = if (isDarkTheme) Color(0xFF1E1C24) else Color.White,
                                radius = 6f,
                                center = point
                            )
                        } else {
                            drawCircle(
                                color = Color(0xFFFFB300),
                                radius = 5.5f,
                                center = point
                            )
                        }

                        // Time label
                        val textLayoutResult = textMeasurer.measure(
                            text = item.timeString,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = labelColor,
                                fontWeight = if (selectedIndex == i) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(x - textLayoutResult.size.width / 2, height - paddingBottom + 6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, textColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
