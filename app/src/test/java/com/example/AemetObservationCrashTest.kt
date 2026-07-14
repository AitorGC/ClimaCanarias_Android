package com.example

import org.junit.Test
import com.example.data.WeatherApiClient
import com.squareup.moshi.Types

class AemetObservationCrashTest {
    @Test
    fun testCrash() {
        try {
            val mapType = Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            val listType = Types.newParameterizedType(
                List::class.java,
                mapType
            )
            val adapter = WeatherApiClient.moshi.adapter<List<Map<String, Any?>>>(listType)
            val json = """[{"fint":"2023-10-10T10:00:00","ubi":"Madrid","ta":20.5,"hr":50}]"""
            val result = adapter.fromJson(json)
            println("No crash: adapter created and parsed: $result")
        } catch (e: Exception) {
            println("Crash: ${e.message}")
            e.printStackTrace()
        }
    }
}
