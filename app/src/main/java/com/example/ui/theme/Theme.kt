package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CanaryYellow,
    secondary = SignalBlue,
    tertiary = CanaryYellow,
    background = Color(0xFF141318),
    surface = Color(0xFF1E1C24),
    onPrimary = Color.Black,
    onSecondary = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SignalBlue,
    secondary = CanaryYellow,
    tertiary = SignalBlue,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF141318),
    onSurface = Color(0xFF141318),
    surfaceVariant = Color(0xFFF4F7FA), // Soft grey-blue for cards and elements
    onSurfaceVariant = Color(0xFF141318),
    outline = SignalBlue,
    outlineVariant = Color(0xFFE2E8F0)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic colors by default so that the gorgeous Gran Canaria signal blue/yellow branding shines through
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
