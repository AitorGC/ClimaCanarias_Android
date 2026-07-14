package com.example

import org.junit.Test
import com.example.data.WeatherApiClient
import com.example.data.AemetStationDto
import com.squareup.moshi.Types

class AemetStationCrashTest {
    @Test
    fun testCrash() {
        try {
            val listType = Types.newParameterizedType(List::class.java, AemetStationDto::class.java)
            val adapter = WeatherApiClient.moshi.adapter<List<AemetStationDto>>(listType)
            println("No crash: adapter created")
        } catch (e: Exception) {
            println("Crash: ${e.message}")
        }
    }
}
