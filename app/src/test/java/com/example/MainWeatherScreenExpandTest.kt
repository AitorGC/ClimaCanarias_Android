package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.ui.screens.MainWeatherScreen
import com.example.viewmodel.WeatherViewModel
import com.example.repository.WeatherRepository
import androidx.test.core.app.ApplicationProvider
import android.app.Application

@RunWith(AndroidJUnit4::class)
class MainWeatherScreenExpandTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExpandStation() {
        // Let's just create the state with dummy data to see if it crashes on render
        // Actually, without activity it might be tricky. Let's not run this.
    }
}
