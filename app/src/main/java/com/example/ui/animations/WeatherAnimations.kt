package com.example.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.WeatherCondition
import kotlin.math.*
import kotlin.random.Random

// Math helper functions returning Float
private inline fun sinF(angle: Double): Float = sin(angle).toFloat()
private inline fun sinF(angle: Float): Float = sin(angle.toDouble()).toFloat()
private inline fun cosF(angle: Double): Float = cos(angle).toFloat()
private inline fun cosF(angle: Float): Float = cos(angle.toDouble()).toFloat()

// Particles representation for dynamic weather elements
private data class WeatherParticle(
    val id: Int,
    val initialX: Float, // percentage 0..1
    val initialY: Float, // percentage 0..1
    val size: Float,
    val speed: Float,
    val angle: Float = 0f,
    val frequency: Float = 1f,
    val amplitude: Float = 1f,
    val opacity: Float = 1f,
    val layer: Int = 0 // 0 = background, 1 = foreground
)

private data class WindStream(
    val id: Int,
    val yRatio: Float,       // 0..1 vertical offset
    val wavelength: Float,   // wave length in px factor
    val amplitude: Float,    // wave height
    val strokeWidth: Float,
    val opacity: Float,
    val speed: Float
)

@Composable
fun WeatherAnimations(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    windSpeedKmh: Double = 0.0
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAnimationsTransition")
    
    // Primary animation state: continuous time progress (0 to 1 over 10 seconds)
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AnimationTimeProgress"
    )

    // Secondary pulsing value for atmospheric glow
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = SineIntensityEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlowAnim"
    )

    // Slow rotation for sun rays (0 to 360 deg over 20 seconds)
    val sunRotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRotation"
    )

    // Lightning trigger for Storm condition
    val lightningTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LightningTrigger"
    )

    // Wind flow phase for breeze lines
    val windPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WindPhase"
    )

    // Pre-generate static particles per weather condition
    val particles = remember(condition) {
        val list = ArrayList<WeatherParticle>()
        val count = when (condition) {
            WeatherCondition.CALIMA -> 50
            WeatherCondition.RAINY -> 45
            WeatherCondition.STORM -> 55
            WeatherCondition.SNOWY -> 35
            WeatherCondition.CLOUDY -> 12
            WeatherCondition.SUNNY -> 16
        }
        val random = Random(condition.ordinal + 42)
        for (i in 0 until count) {
            val layer = if (i % 3 == 0) 1 else 0
            list.add(
                WeatherParticle(
                    id = i,
                    initialX = random.nextFloat(),
                    initialY = random.nextFloat(),
                    size = when (condition) {
                        WeatherCondition.CALIMA -> 2.5f + random.nextFloat() * 4.5f
                        WeatherCondition.RAINY, WeatherCondition.STORM -> if (layer == 1) 3f + random.nextFloat() * 2f else 1.8f + random.nextFloat() * 1.5f
                        WeatherCondition.SNOWY -> 3.5f + random.nextFloat() * 5.5f
                        WeatherCondition.CLOUDY -> 35f + random.nextFloat() * 45f
                        WeatherCondition.SUNNY -> 14f + random.nextFloat() * 28f // bokeh light spheres
                    },
                    speed = 0.15f + random.nextFloat() * 0.85f,
                    frequency = 1.5f + random.nextFloat() * 2.5f,
                    amplitude = 0.02f + random.nextFloat() * 0.06f,
                    opacity = 0.35f + random.nextFloat() * 0.55f,
                    layer = layer
                )
            )
        }
        list
    }

    // Pre-generate wind streams
    val windStreams = remember {
        listOf(
            WindStream(0, yRatio = 0.22f, wavelength = 180f, amplitude = 12f, strokeWidth = 2.2f, opacity = 0.45f, speed = 1.0f),
            WindStream(1, yRatio = 0.45f, wavelength = 220f, amplitude = 16f, strokeWidth = 2.8f, opacity = 0.60f, speed = 1.25f),
            WindStream(2, yRatio = 0.68f, wavelength = 160f, amplitude = 10f, strokeWidth = 1.8f, opacity = 0.38f, speed = 0.85f),
            WindStream(3, yRatio = 0.82f, wavelength = 200f, amplitude = 14f, strokeWidth = 2.4f, opacity = 0.50f, speed = 1.10f)
        )
    }

    val isHighWind = windSpeedKmh >= 18.0

    Box(modifier = modifier) {
        // --- 1. Ambient Background Atmosphere Overlays ---
        when (condition) {
            WeatherCondition.SUNNY -> {
                // Expanding gentle radial sun glow in top-right corner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F).copy(alpha = pulseGlow * 0.45f),
                                    Color(0xFFFF9800).copy(alpha = pulseGlow * 0.18f),
                                    Color.Transparent
                                ),
                                center = Offset(0.82f, 0.18f),
                                radius = 650f
                            )
                        )
                )
            }
            WeatherCondition.RAINY -> {
                // Soft blue-cyan ambient rain mist
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0288D1).copy(alpha = pulseGlow * 0.20f),
                                    Color(0xFF01579B).copy(alpha = pulseGlow * 0.10f)
                                )
                            )
                        )
                )
            }
            WeatherCondition.STORM -> {
                // Subtle lightning simulation overlay
                val isLightningSession = lightningTrigger > 0.82f && lightningTrigger < 0.94f
                if (isLightningSession) {
                    val opacity = if (lightningTrigger * 100 % 12 < 6) 0.38f else 0.12f
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0F7FA).copy(alpha = opacity))
                    )
                }
            }
            WeatherCondition.CALIMA -> {
                // Sahara dust warm amber atmospheric haze
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE5A93B).copy(alpha = pulseGlow * 0.50f),
                                    Color(0xFF8D6E63).copy(alpha = pulseGlow * 0.35f),
                                    Color(0xFF4E342E).copy(alpha = pulseGlow * 0.20f)
                                )
                            )
                        )
                )
            }
            WeatherCondition.CLOUDY -> {
                // Subtle overcast silvery-blue sheen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF90A4AE).copy(alpha = pulseGlow * 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            WeatherCondition.SNOWY -> {
                // Cold crisp blue snow shimmer gradient at bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFE0F7FA).copy(alpha = pulseGlow * 0.25f)
                                )
                            )
                        )
                )
            }
        }

        // --- 2. High Performance Dedicated Canvas Drawing ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width == 0f || height == 0f) return@Canvas

            when (condition) {
                WeatherCondition.SUNNY -> {
                    val sunCenter = Offset(width * 0.82f, height * 0.22f)

                    // A) Floating Golden Bokeh / Light Spheres rising upward
                    for (p in particles) {
                        val progressY = (p.initialY - animationProgress * p.speed * 0.5f + 1.0f) % 1.0f
                        val currentY = progressY * height
                        val oscillateX = p.amplitude * width * sinF(2 * PI * (progressY * p.frequency + p.initialX))
                        val currentX = (p.initialX * width + oscillateX) % width

                        val particleAlpha = (sinF(progressY * PI) * p.opacity).coerceIn(0.1f, 0.75f)
                        val dynamicRadius = p.size * (0.8f + 0.4f * sinF(2 * PI * (animationProgress + p.id)))

                        drawCircle(
                            color = Color(0xFFFFECB3).copy(alpha = particleAlpha),
                            radius = dynamicRadius,
                            center = Offset(currentX, currentY)
                        )
                    }

                    // B) Dual Rotating Solar Rays Wheel (Concentric opposite rotation)
                    rotate(degrees = sunRotationDegrees, pivot = sunCenter) {
                        for (i in 0 until 12) {
                            val rayAngleRad = (i * 30f * PI / 180f).toFloat()
                            val pulseLen = 50f + 15f * sinF(2 * PI * animationProgress + i)

                            val startOffset = Offset(
                                sunCenter.x + 36f * cosF(rayAngleRad),
                                sunCenter.y + 36f * sinF(rayAngleRad)
                            )
                            val endOffset = Offset(
                                sunCenter.x + (36f + pulseLen) * cosF(rayAngleRad),
                                sunCenter.y + (36f + pulseLen) * sinF(rayAngleRad)
                            )

                            drawLine(
                                color = Color(0xFFFFB300).copy(alpha = 0.55f),
                                start = startOffset,
                                end = endOffset,
                                strokeWidth = 3.8f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    rotate(degrees = -sunRotationDegrees * 0.6f, pivot = sunCenter) {
                        for (i in 0 until 8) {
                            val rayAngleRad = (i * 45f * PI / 180f).toFloat()
                            val startOffset = Offset(
                                sunCenter.x + 48f * cosF(rayAngleRad),
                                sunCenter.y + 48f * sinF(rayAngleRad)
                            )
                            val endOffset = Offset(
                                sunCenter.x + 85f * cosF(rayAngleRad),
                                sunCenter.y + 85f * sinF(rayAngleRad)
                            )

                            drawLine(
                                color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                                start = startOffset,
                                end = endOffset,
                                strokeWidth = 2.2f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // C) Central Glowing Sun Disc & Corona
                    drawCircle(
                        color = Color(0xFFFFF8E1).copy(alpha = 0.45f),
                        radius = 46f,
                        center = sunCenter
                    )
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 32f,
                        center = sunCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 22f,
                        center = sunCenter
                    )
                }

                WeatherCondition.RAINY, WeatherCondition.STORM -> {
                    val isStorm = condition == WeatherCondition.STORM
                    val baseAngle = if (isStorm) -28f else -10f
                    val speedScale = if (isStorm) 2.2f else 1.25f

                    for (p in particles) {
                        val progressY = (p.initialY + animationProgress * p.speed * speedScale) % 1.0f
                        val currentY = progressY * height

                        val angleOffset = baseAngle + (p.amplitude * 50f)
                        val currentX = (p.initialX * width + (progressY * width * (angleOffset / 100f))) % width

                        val dropColor = if (p.layer == 1) {
                            Color(0xFF80DEEA).copy(alpha = 0.85f)
                        } else {
                            Color(0xFF29B6F6).copy(alpha = 0.45f)
                        }

                        val strokeW = if (p.layer == 1) 2.8f else 1.6f
                        val dropLen = p.size * (if (isStorm) 4.5f else 3.2f)

                        // Draw Rain Streak
                        drawLine(
                            color = dropColor,
                            start = Offset(currentX, currentY),
                            end = Offset(
                                currentX + (angleOffset * 0.35f),
                                currentY + dropLen
                            ),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )

                        // Ground Splash Rings near bottom of canvas
                        if (currentY > height * 0.78f) {
                            val rippleProgress = (currentY - height * 0.78f) / (height * 0.22f)
                            val rippleRadius = 4f + rippleProgress * 14f
                            val rippleAlpha = (1.0f - rippleProgress) * 0.6f

                            drawOval(
                                color = Color(0xFFB2EBF2).copy(alpha = rippleAlpha.coerceIn(0f, 1f)),
                                topLeft = Offset(currentX - rippleRadius, currentY - rippleRadius * 0.35f),
                                size = Size(rippleRadius * 2f, rippleRadius * 0.7f),
                                style = Stroke(width = 1.2f)
                            )
                        }
                    }

                    // D) Storm Lightning Bolts Path
                    if (isStorm && lightningTrigger > 0.84f && lightningTrigger < 0.92f) {
                        val boltPath = Path().apply {
                            moveTo(width * 0.70f, 0f)
                            lineTo(width * 0.62f, height * 0.28f)
                            lineTo(width * 0.67f, height * 0.32f)
                            lineTo(width * 0.52f, height * 0.65f)
                            lineTo(width * 0.58f, height * 0.68f)
                            lineTo(width * 0.45f, height * 0.95f)
                        }

                        // Glow
                        drawPath(
                            path = boltPath,
                            color = Color(0xFFE0F7FA).copy(alpha = 0.85f),
                            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        // Core bolt
                        drawPath(
                            path = boltPath,
                            color = Color.White,
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                WeatherCondition.CALIMA -> {
                    // Saharan Dust / Fine Sand Grains flowing horizontally
                    for (p in particles) {
                        val progressX = (p.initialX + animationProgress * p.speed * 1.1f) % 1.0f
                        val currentX = progressX * width

                        val sineOffset = p.amplitude * height * sinF(2 * PI * (progressX * p.frequency + p.initialY))
                        val currentY = (p.initialY * height + sineOffset).coerceIn(0f, height)

                        val dustColor = if (p.layer == 1) {
                            Color(0xFFFFE082).copy(alpha = 0.80f)
                        } else {
                            Color(0xFFBCAAA4).copy(alpha = 0.50f)
                        }

                        drawCircle(
                            color = dustColor,
                            radius = p.size,
                            center = Offset(currentX, currentY)
                        )
                    }

                    // Soft Saharan Haze Cloud Blobs drifting
                    for (i in 0 until 3) {
                        val cloudProgress = (animationProgress * 0.08f + i * 0.33f) % 1.0f
                        val cloudX = cloudProgress * width
                        val cloudY = height * (0.25f + i * 0.25f)

                        drawOval(
                            color = Color(0xFFD7CCC8).copy(alpha = 0.18f),
                            topLeft = Offset(cloudX - 120f, cloudY - 40f),
                            size = Size(240f, 80f)
                        )
                    }
                }

                WeatherCondition.CLOUDY -> {
                    // Multi-depth Cloud Silhouettes drifting across canvas
                    for (p in particles) {
                        val progressX = (p.initialX + animationProgress * p.speed * 0.18f) % 1.0f
                        val currentX = progressX * width
                        val currentY = p.initialY * height * 0.65f

                        // Cloud blob shape
                        drawOval(
                            color = Color.White.copy(alpha = p.opacity * 0.35f),
                            topLeft = Offset(currentX - p.size, currentY - p.size * 0.45f),
                            size = Size(p.size * 2.2f, p.size * 1.1f)
                        )
                        // Silver lining top edge highlight
                        drawOval(
                            color = Color(0xFFECEFF1).copy(alpha = p.opacity * 0.55f),
                            topLeft = Offset(currentX - p.size * 0.8f, currentY - p.size * 0.5f),
                            size = Size(p.size * 1.6f, p.size * 0.4f)
                        )
                    }
                }

                WeatherCondition.SNOWY -> {
                    // Floating snowflakes oscillating with lateral sway
                    for (p in particles) {
                        val progressY = (p.initialY + animationProgress * p.speed * 0.32f) % 1.0f
                        val currentY = progressY * height

                        val oscillateX = p.amplitude * width * sinF(2 * PI * (progressY * p.frequency + p.initialX))
                        val currentX = (p.initialX * width + oscillateX) % width

                        drawCircle(
                            color = Color.White.copy(alpha = p.opacity),
                            radius = p.size * 0.5f,
                            center = Offset(currentX, currentY)
                        )
                    }
                }
            }

            // --- 3. Flowing Wind Currents Overlay (For High Wind or Windy conditions) ---
            if (isHighWind || condition == WeatherCondition.CALIMA) {
                val windColor = if (condition == WeatherCondition.CALIMA) {
                    Color(0xFFFFECB3).copy(alpha = 0.35f)
                } else {
                    Color.White.copy(alpha = 0.40f)
                }

                for (stream in windStreams) {
                    val streamY = height * stream.yRatio
                    val path = Path()

                    val startX = -100f
                    val endX = width + 100f

                    path.moveTo(startX, streamY)

                    var x = startX
                    val step = 30f
                    while (x <= endX) {
                        val sineVal = sinF((x / stream.wavelength) + windPhase * stream.speed)
                        val y = streamY + (stream.amplitude * sineVal)
                        path.lineTo(x, y)
                        x += step
                    }

                    drawPath(
                        path = path,
                        color = windColor.copy(alpha = stream.opacity),
                        style = Stroke(width = stream.strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

// Custom easing supporting smooth sine intensity pulse curves
private val SineIntensityEasing = Easing { fraction ->
    sinF(fraction * (PI / 2))
}
