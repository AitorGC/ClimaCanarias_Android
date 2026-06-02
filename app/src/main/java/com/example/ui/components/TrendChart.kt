package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HourlyForecastItem
import kotlin.math.max
import kotlin.math.min

@Composable
fun TrendChart(
    hourlyItems: List<HourlyForecastItem>,
    isCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    if (hourlyItems.isEmpty()) return

    // Limit to 12 items (e.g., every 2 hours) to avoid overcrowding in compact screens
    val chartItems = remember(hourlyItems) {
        if (hourlyItems.size > 12) {
            hourlyItems.filterIndexed { index, _ -> index % 2 == 0 }.take(12)
        } else {
            hourlyItems
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val themePrimaryColor = MaterialTheme.colorScheme.primary

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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tendencias Próximas (24h)",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(color = Color(0xFFFFB300), label = "Temp")
                    LegendItem(color = Color(0xFF26A69A), label = "Humedad")
                    LegendItem(color = Color(0xFF29B6F6), label = "% Lluvia")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tooltip preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedIndex != null && selectedIndex!! < chartItems.size) {
                    val item = chartItems[selectedIndex!!]
                    val displayTemp = if (isCelsius) {
                        "${item.temperature}°C"
                    } else {
                        "${String.format("%.1f", item.temperature * 9 / 5 + 32)}F"
                    }
                    Text(
                        text = "Hora: ${item.timeString} ➔ Temp: $displayTemp | Hum: ${item.humidity.toInt()}% | Lluvia: ${item.precipitationProbability}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = "Desliza o toca sobre el gráfico para ver detalles por hora",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                                val xStep = size.width / (chartItems.size - 1)
                                val index = (offset.x / xStep).toInt().coerceIn(0, chartItems.size - 1)
                                selectedIndex = index
                            }
                        }
                        .pointerInput(chartItems) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val xStep = size.width / (chartItems.size - 1)
                                    val index = (offset.x / xStep).toInt().coerceIn(0, chartItems.size - 1)
                                    selectedIndex = index
                                },
                                onDragEnd = { selectedIndex = null },
                                onDragCancel = { selectedIndex = null },
                                onDrag = { change, _ ->
                                    val xStep = size.width / (chartItems.size - 1)
                                    val index = (change.position.x / xStep).toInt().coerceIn(0, chartItems.size - 1)
                                    selectedIndex = index
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    
                    if (width == 0f || height == 0f) return@Canvas

                    val paddingBottom = 25f
                    val paddingTop = 15f
                    val graphHeight = height - paddingBottom - paddingTop
                    
                    val temps = chartItems.map { it.temperature }
                    val minTemp = temps.minOrNull() ?: 15.0
                    val maxTemp = temps.maxOrNull() ?: 35.0
                    val tempRange = if (maxTemp == minTemp) 1.0 else maxTemp - minTemp

                    val stepX = width / (chartItems.size - 1)

                    // 1. Draw grid background lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val gridY = paddingTop + (graphHeight * i / gridLines)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Draw precipitation probability as elegant translucent rounded bars
                    chartItems.forEachIndexed { i, item ->
                        val barWidth = stepX * 0.35f
                        val barHeight = graphHeight * (item.precipitationProbability / 100f)
                        val barX = i * stepX - barWidth / 2
                        val barY = paddingTop + graphHeight - barHeight

                        drawRoundRect(
                            color = Color(0xFF29B6F6).copy(alpha = 0.3f),
                            topLeft = Offset(barX, barY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }

                    // 3. Draw humidity as a dotted green line
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
                        color = Color(0xFF26A69A),
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    // 4. Draw temperature as a gorgeous smooth bezier path with solid gradient fill below it
                    val tempPoints = chartItems.mapIndexed { i, item ->
                        val norm = (item.temperature - minTemp) / tempRange
                        val y = paddingTop + graphHeight - (graphHeight * norm).toFloat()
                        Offset(i * stepX, y)
                    }

                    val tempPath = Path().apply {
                        if (tempPoints.isNotEmpty()) {
                            moveTo(tempPoints[0].x, tempPoints[0].y)
                            for (i in 0 until tempPoints.size - 1) {
                                val p0 = tempPoints[i]
                                val p1 = tempPoints[i + 1]
                                // Bezier control coordinates
                                val controlX = (p0.x + p1.x) / 2
                                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                        }
                    }

                    // Draw gradient below temperature curve
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
                                Color(0xFFFFB300).copy(alpha = 0.35f),
                                Color(0xFFFFB300).copy(alpha = 0.0f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + graphHeight
                        )
                    )

                    // Draw temperature path line
                    drawPath(
                        path = tempPath,
                        color = Color(0xFFFFB300),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )

                    // 5. Draw labels at the bottom (Time) and temperature markers
                    chartItems.forEachIndexed { i, item ->
                        val x = i * stepX
                        
                        // Vertical guideline for currently selected index
                        if (selectedIndex == i) {
                            drawLine(
                                color = themePrimaryColor.copy(alpha = 0.6f),
                                start = Offset(x, paddingTop),
                                end = Offset(x, paddingTop + graphHeight),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                            
                            // Highlight dot for selected temp point
                            drawCircle(
                                color = Color(0xFFFFB300),
                                radius = 14f,
                                center = tempPoints[i]
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 8f,
                                center = tempPoints[i]
                            )
                        } else {
                            // Small normal anchor dots
                            drawCircle(
                                color = Color(0xFFFFB300),
                                radius = 7f,
                                center = tempPoints[i]
                            )
                        }

                        // Time label
                        val textLayoutResult = textMeasurer.measure(
                            text = item.timeString,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(x - textLayoutResult.size.width / 2, height - paddingBottom + 4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}
