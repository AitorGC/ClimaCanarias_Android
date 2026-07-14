package com.example

import org.junit.Test
import com.example.data.WeatherApiClient
import com.example.data.AemetStationDto
import com.squareup.moshi.Types

class AemetStationParseTest {
    @Test
    fun testParse() {
        try {
            val listType = Types.newParameterizedType(List::class.java, AemetStationDto::class.java)
            val adapter = WeatherApiClient.moshi.adapter<List<AemetStationDto>>(listType)
            val json = """[{"latitud":"28.123","provincia":"LAS PALMAS","altitud":10,"indicativo":"C123","nombre":"Test","longitud":"-15.123","indsinop":"123"}]"""
            val result = adapter.fromJson(json)
            println("No crash: parsed: $result")
        } catch (e: Exception) {
            println("Crash: ${e.message}")
            e.printStackTrace()
        }
    }
}
