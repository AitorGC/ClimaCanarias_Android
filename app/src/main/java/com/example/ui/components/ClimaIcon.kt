package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ClimaIcon(
    name: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Gray
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val width = size.width
        val height = size.height

        when (name) {
            "sunny" -> {
                // Sun Core
                drawCircle(color = tint, radius = width * 0.3f, center = Offset(width * 0.5f, height * 0.5f))
                // Rays
                for (i in 0 until 8) {
                    val angle = i * Math.PI / 4
                    val startX = (width * 0.5f + width * 0.36f * Math.cos(angle)).toFloat()
                    val startY = (height * 0.5f + height * 0.36f * Math.sin(angle)).toFloat()
                    val endX = (width * 0.5f + width * 0.48f * Math.cos(angle)).toFloat()
                    val endY = (height * 0.5f + height * 0.48f * Math.sin(angle)).toFloat()
                    drawLine(color = tint, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2.5f, cap = StrokeCap.Round)
                }
            }
            "cloudy" -> {
                // Cloud outline/filled puff
                val path = Path().apply {
                    moveTo(width * 0.3f, height * 0.7f)
                    cubicTo(width * 0.15f, height * 0.7f, width * 0.15f, height * 0.45f, width * 0.3f, height * 0.45f)
                    cubicTo(width * 0.3f, height * 0.25f, width * 0.6f, height * 0.25f, width * 0.6f, height * 0.45f)
                    cubicTo(width * 0.8f, height * 0.45f, width * 0.82f, height * 0.7f, width * 0.7f, height * 0.7f)
                    close()
                }
                drawPath(path = path, color = tint)
            }
            "rainy" -> {
                // Cloud at top, lines at bottom
                val path = Path().apply {
                    moveTo(width * 0.35f, height * 0.55f)
                    cubicTo(width * 0.2f, height * 0.55f, width * 0.2f, height * 0.35f, width * 0.35f, height * 0.35f)
                    cubicTo(width * 0.35f, height * 0.18f, width * 0.65f, height * 0.18f, width * 0.65f, height * 0.35f)
                    cubicTo(width * 0.8f, height * 0.35f, width * 0.8f, height * 0.55f, width * 0.7f, height * 0.55f)
                    close()
                }
                drawPath(path = path, color = tint)
                // Rainfall marks
                drawLine(color = tint, start = Offset(width * 0.4f, height * 0.65f), end = Offset(width * 0.35f, height * 0.85f), strokeWidth = 2.5f)
                drawLine(color = tint, start = Offset(width * 0.6f, height * 0.65f), end = Offset(width * 0.55f, height * 0.85f), strokeWidth = 2.5f)
            }
            "snowy" -> {
                // Cross design snowflake
                drawLine(color = tint, start = Offset(width * 0.5f, height * 0.15f), end = Offset(width * 0.5f, height * 0.85f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.15f, height * 0.5f), end = Offset(width * 0.85f, height * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.25f, height * 0.25f), end = Offset(width * 0.75f, height * 0.75f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.25f, height * 0.75f), end = Offset(width * 0.75f, height * 0.25f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
            "storm" -> {
                // Lightning bolt
                val path = Path().apply {
                    moveTo(width * 0.55f, height * 0.15f)
                    lineTo(width * 0.25f, height * 0.55f)
                    lineTo(width * 0.52f, height * 0.55f)
                    lineTo(width * 0.38f, height * 0.88f)
                    lineTo(width * 0.75f, height * 0.42f)
                    lineTo(width * 0.48f, height * 0.42f)
                    close()
                }
                drawPath(path = path, color = tint)
            }
            "calima" -> {
                // Waves/Wind lines mimicking dust suspension
                drawLine(color = tint, start = Offset(width * 0.15f, height * 0.35f), end = Offset(width * 0.85f, height * 0.35f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.25f, height * 0.5f), end = Offset(width * 0.75f, height * 0.5f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.15f, height * 0.65f), end = Offset(width * 0.85f, height * 0.65f), strokeWidth = 3f, cap = StrokeCap.Round)
            }
            "humidity" -> {
                // Drop shape
                val path = Path().apply {
                    moveTo(width * 0.5f, height * 0.15f)
                    cubicTo(width * 0.25f, height * 0.55f, width * 0.25f, height * 0.85f, width * 0.5f, height * 0.85f)
                    cubicTo(width * 0.75f, height * 0.85f, width * 0.75f, height * 0.55f, width * 0.5f, height * 0.15f)
                    close()
                }
                drawPath(path = path, color = tint)
            }
            "wind" -> {
                // Horizontal swirl wind indicator
                drawLine(color = tint, start = Offset(width * 0.15f, height * 0.35f), end = Offset(width * 0.65f, height * 0.35f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.25f, height * 0.5f), end = Offset(width * 0.85f, height * 0.5f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawLine(color = tint, start = Offset(width * 0.15f, height * 0.65f), end = Offset(width * 0.55f, height * 0.65f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
            "gps" -> {
                // Position pin
                drawCircle(color = tint, radius = width * 0.24f, center = Offset(width * 0.5f, height * 0.44f))
                val path = Path().apply {
                    moveTo(width * 0.5f, height * 0.88f)
                    lineTo(width * 0.32f, height * 0.55f)
                    lineTo(width * 0.68f, height * 0.55f)
                    close()
                }
                drawPath(path = path, color = tint)
            }
            "light_mode" -> {
                // Sun Core with simple thin ring
                drawCircle(color = tint, radius = width * 0.22f, center = Offset(width * 0.5f, height * 0.5f))
                for (i in 0 until 8) {
                    val angle = i * Math.PI / 4
                    val startX = (width * 0.5f + width * 0.3f * Math.cos(angle)).toFloat()
                    val startY = (height * 0.5f + height * 0.3f * Math.sin(angle)).toFloat()
                    val endX = (width * 0.5f + width * 0.42f * Math.cos(angle)).toFloat()
                    val endY = (height * 0.5f + height * 0.42f * Math.sin(angle)).toFloat()
                    drawLine(color = tint, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = 2f, cap = StrokeCap.Round)
                }
            }
            "dark_mode" -> {
                // Crescent Moon shape
                val path = Path().apply {
                    moveTo(width * 0.7f, height * 0.25f)
                    cubicTo(width * 0.35f, height * 0.25f, width * 0.3f, height * 0.65f, width * 0.55f, height * 0.8f)
                    cubicTo(width * 0.72f, height * 0.8f, width * 0.8f, height * 0.7f, width * 0.85f, height * 0.6f)
                    cubicTo(width * 0.55f, height * 0.65f, width * 0.5f, height * 0.4f, width * 0.7f, height * 0.25f)
                    close()
                }
                drawPath(path = path, color = tint)
            }
            "disconnected" -> {
                // Simulating offline status icon: Warning inside a circle
                drawCircle(color = tint.copy(alpha = 0.15f), radius = width * 0.45f, center = Offset(width * 0.5f, height * 0.5f))
                drawLine(color = tint, start = Offset(width * 0.5f, height * 0.25f), end = Offset(width * 0.5f, height * 0.6f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawCircle(color = tint, radius = 2.5f, center = Offset(width * 0.5f, height * 0.75f))
            }
        }
    }
}
