package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainWeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WeatherViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  private val viewModel: WeatherViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        var backPressedOnce by remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        LaunchedEffect(backPressedOnce) {
            if (backPressedOnce) {
                delay(2000)
                backPressedOnce = false
            }
        }
        
        BackHandler(enabled = true) {
            if (backPressedOnce) {
                finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Presiona una vez más para salir", Toast.LENGTH_SHORT).show()
            }
        }

        val targetTab = intent?.getIntExtra("TARGET_TAB", 0) ?: 0

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainWeatherScreen(viewModel = viewModel, initialPage = targetTab)
        }
      }
    }
  }
}
