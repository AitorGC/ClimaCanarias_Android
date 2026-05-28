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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.data.WeatherCondition
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// Particles representation
private data class Particle(
    val id: Int,
    val initialX: Float, // percentage 0..1
    val initialY: Float, // percentage 0..1
    val size: Float,
    val speed: Float,
    val angle: Float = 0f,
    val frequency: Float = 1f,
    val amplitude: Float = 1f
)

@Composable
fun WeatherAnimations(
    condition: WeatherCondition,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherAnimationsTransition")
    
    // Primary animation state: a timer progress running from 0 to 1
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AnimationTimeProgress"
    )

    // Secondary pulsing value for glow
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = SineIntensityEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlowAnim"
    )

    // Lightning flash simulator (for Storm)
    val lightningTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LightningTrigger"
    )

    // Pre-generate static particles to prevent re-generation during render cycles
    val particles = remember(condition) {
        val list = ArrayList<Particle>()
        val count = when (condition) {
            WeatherCondition.CALIMA -> 35
            WeatherCondition.RAINY, WeatherCondition.STORM -> 40
            WeatherCondition.SNOWY -> 30
            WeatherCondition.CLOUDY -> 8
            WeatherCondition.SUNNY -> 6
        }
        val random = Random(condition.ordinal)
        for (i in 0 until count) {
            list.add(
                Particle(
                    id = i,
                    initialX = random.nextFloat(),
                    initialY = random.nextFloat(),
                    size = when (condition) {
                        WeatherCondition.CALIMA -> 3f + random.nextFloat() * 4f
                        WeatherCondition.RAINY, WeatherCondition.STORM -> 2f + random.nextFloat() * 2.5f
                        WeatherCondition.SNOWY -> 4f + random.nextFloat() * 6f
                        WeatherCondition.CLOUDY -> 40f + random.nextFloat() * 50f
                        WeatherCondition.SUNNY -> 30f + random.nextFloat() * 40f
                    },
                    speed = 0.1f + random.nextFloat() * 0.9f,
                    frequency = 2f + random.nextFloat() * 3f,
                    amplitude = 0.02f + random.nextFloat() * 0.05f
                )
            )
        }
        list
    }

    Box(modifier = modifier) {
        // Ambient background overlays depending on current weather status
        when (condition) {
            WeatherCondition.CALIMA -> {
                // Dim dust orange glow background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE5A93B).copy(alpha = pulseGlow * 0.5f),
                                    Color(0xFF8D6E63).copy(alpha = pulseGlow * 0.3f)
                                )
                            )
                        )
                )
            }
            WeatherCondition.STORM -> {
                // Subtle lightning simulation
                val isLightningSession = lightningTrigger > 0.85f && lightningTrigger < 0.95f
                if (isLightningSession) {
                    val lightningOpacity = if (lightningTrigger * 100 % 10 < 5) 0.35f else 0.1f
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = lightningOpacity))
                    )
                }
            }
            WeatherCondition.SUNNY -> {
                // Expanding gentle radial sun rays background glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F).copy(alpha = pulseGlow * 0.3f),
                                    Color.Transparent
                                ),
                                radius = 450f
                            )
                        )
                )
            }
            else -> {}
        }

        // Dedicated Canvas drawings for moving weather patterns
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            if (width == 0f || height == 0f) return@Canvas

            when (condition) {
                WeatherCondition.CALIMA -> {
                    // Microscopic sand particles: moving horizontally from left to right along sinusoidal waveforms
                    for (p in particles) {
                        // Horizontal displacement loops around
                        val progressX = (p.initialX + animationProgress * p.speed) % 1.0f
                        val currentX = progressX * width
                        
                        // Vertical sine swing
                        val sineOffset = p.amplitude * height * sin(2 * PI * (progressX * p.frequency + p.initialY))
                        val currentY = (p.initialY * height + sineOffset).toFloat().coerceIn(0f, height)
                        
                        // Draw fine grains of sand
                        drawCircle(
                            color = Color(0xFFFDD835).copy(alpha = 0.75f),
                            radius = p.size,
                            center = Offset(currentX, currentY)
                        )
                    }
                }
                
                WeatherCondition.RAINY, WeatherCondition.STORM -> {
                    val isStorm = condition == WeatherCondition.STORM
                    val angleOffset = if (isStorm) -25f else -5f // More windy slant in storm representation
                    val speedScale = if (isStorm) 1.8f else 1.1f
                    
                    for (p in particles) {
                        val progressY = (p.initialY + animationProgress * p.speed * speedScale) % 1.0f
                        val currentY = progressY * height
                        
                        // Slightly slanted rainfall
                        val currentX = (p.initialX * width + (progressY * width * (angleOffset / 100f))) % width
                        
                        // Draw drop as a styled fine slanted line
                        drawLine(
                            color = Color(0xFF29B6F6).copy(alpha = 0.65f),
                            start = Offset(currentX, currentY),
                            end = Offset(
                                currentX + (angleOffset * 0.4f).toFloat(), 
                                currentY + p.size * 3.5f
                            ),
                            strokeWidth = 2.5f
                        )
                    }
                }
                
                WeatherCondition.CLOUDY -> {
                    // Cloudy: mass of low opacity blobs slowly drifting
                    for (p in particles) {
                        val progressX = (p.initialX + animationProgress * p.speed * 0.15f) % 1.0f
                        val currentX = progressX * width
                        val currentY = p.initialY * height * 0.6f // mostly in upper range of card

                        drawOval(
                            color = Color.White.copy(alpha = 0.25f),
                            topLeft = Offset(currentX - p.size, currentY - p.size / 2),
                            size = Size(p.size * 2, p.size * 1.2f)
                        )
                    }
                }

                WeatherCondition.SNOWY -> {
                    // Snowflake circles falling and oscillating on X
                    for (p in particles) {
                        val progressY = (p.initialY + animationProgress * p.speed * 0.3f) % 1.0f
                        val currentY = progressY * height
                        
                        // Oscillate left-right
                        val oscillateX = p.amplitude * width * sin(2 * PI * (progressY * p.frequency + p.initialX))
                        val currentX = (p.initialX * width + oscillateX).toFloat() % width
                        
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = p.size / 2,
                            center = Offset(currentX, currentY)
                        )
                    }
                }

                WeatherCondition.SUNNY -> {
                    // Sunny: Radial sunburst expansion
                    val centerSun = Offset(width * 0.8f, height * 0.2f)
                    
                    rotate(degrees = animationProgress * 360f, pivot = centerSun) {
                        // Drawing decorative solar rays extending outwards
                        for (i in 0 until 12) {
                            val rayAngleDegrees = (i * 30f)
                            val rayAngleRad = (rayAngleDegrees * PI / 180f).toFloat()
                            
                            val lengthShort = 45f + 10f * sin((animationProgress * 2 * PI + i).toFloat())
                            val lengthLong = 75f + 15f * sin((animationProgress * 2 * PI + i).toFloat())
                            
                            val startOffset = Offset(
                                centerSun.x + 35f * kotlin.math.cos(rayAngleRad),
                                centerSun.y + 35f * sin(rayAngleRad)
                            )
                            val endOffset = Offset(
                                centerSun.x + lengthLong * kotlin.math.cos(rayAngleRad),
                                centerSun.y + lengthLong * sin(rayAngleRad)
                            )
                            
                            drawLine(
                                color = Color(0xFFFFB300).copy(alpha = 0.55f),
                                start = startOffset,
                                end = endOffset,
                                strokeWidth = 4f
                            )
                        }
                    }
                    
                    // Central bright sun orb in corner
                    drawCircle(
                        color = Color(0xFFFFD54F),
                        radius = 28f,
                        center = centerSun
                    )
                }
            }
        }
    }
}

// Custom easing supporting sine intensity pulse curve
private val SineIntensityEasing = Easing { fraction ->
    sin(fraction * PI / 2).toFloat()
}
