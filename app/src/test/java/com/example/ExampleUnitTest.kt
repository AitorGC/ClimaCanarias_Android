package com.example

import org.junit.Assert.assertNotNull
import org.junit.Test
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    org.junit.Assert.assertEquals(4, 2 + 2)
  }

  @Test
  fun testAemetParsing() {
    val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()
    val adapter = moshi.adapter(com.example.data.AemetObservationDto::class.java)
    
    // This simulates a valid JSON observation from AEMET
    val json = """
        {
            "fint": "2026-07-14T11:00:00",
            "ubi": "LAS PALMAS",
            "ta": 22.4,
            "hr": 68.0,
            "vv": 4.2,
            "dv": 360.0,
            "pres": 1013.2,
            "prec": 0.0
        }
    """.trimIndent()
    
    try {
        val dto = adapter.fromJson(json)
        println("Successfully parsed DTO: ${dto}")
    } catch (e: Exception) {
        println("FAILED to parse DTO: ${e.message}")
        e.printStackTrace()
        org.junit.Assert.fail("Moshi failed to parse DTO: ${e.message}")
    }
  }

  @Test
  fun testRealAemetObservation() {
    val apiKey = System.getenv("AEMET_API_KEY") ?: com.example.BuildConfig.AEMET_API_KEY
    if (apiKey == "YOUR_AEMET_API_KEY" || apiKey.isNullOrBlank()) {
        println("AEMET API KEY IS EMPTY OR DEFAULT!")
        return
    }
    println("Using API Key: $apiKey")
    val client = OkHttpClient()
    
    // 1. Try inventario to see if the key is valid
    val inventarioUrl = "https://opendata.aemet.es/opendata/api/valores/climatologicos/inventarioestaciones/todasestaciones?api_key=$apiKey"
    println("Requesting Inventario URL: $inventarioUrl")
    val requestInventario = Request.Builder().url(inventarioUrl).build()
    try {
        val res = client.newCall(requestInventario).execute().use { it.body?.string() }
        println("Inventario response: ${res?.take(300)}")
    } catch (e: Exception) {
        println("Inventario failed: ${e.message}")
    }

    // 2. Try observation for "C449C" with api_key in header instead of query param
    val obsHeaderUrl = "https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/C449C"
    println("Requesting Observation with Header: $obsHeaderUrl")
    val requestObsHeader = Request.Builder()
        .url(obsHeaderUrl)
        .addHeader("api_key", apiKey)
        .build()
    try {
        val res = client.newCall(requestObsHeader).execute().use { it.body?.string() }
        println("Observation with Header response: $res")
    } catch (e: Exception) {
        println("Observation with Header failed: ${e.message}")
    }

    // 3. Try observation with lowercase "api_key" query parameter
    val obsQueryUrl = "https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/C449C?api_key=$apiKey"
    println("Requesting Observation with Query Param: $obsQueryUrl")
    val requestObsQuery = Request.Builder().url(obsQueryUrl).build()
    try {
        val res = client.newCall(requestObsQuery).execute().use { it.body?.string() }
        println("Observation with Query response: $res")
    } catch (e: Exception) {
        println("Observation with Query failed: ${e.message}")
    }
  }

  @Test
  fun testBeachMatch() {
    val client = OkHttpClient()
    val requestBeach = Request.Builder()
        .url("https://www3.gobiernodecanarias.org/aplicaciones/infoplayas/socorrismo/api/beach")
        .build()

    val beachesJson = client.newCall(requestBeach).execute().use { response ->
        response.body?.string()
    }

    assertNotNull(beachesJson)

    val csvFile = File("src/main/assets/playas.csv")
    if (csvFile.exists()) {
        val lines = csvFile.readLines().drop(1)
        val csvBeaches = lines.mapNotNull { line ->
            val tokens = line.split(";")
            val name = tokens.getOrNull(3)?.trim() ?: ""
            val id = tokens.getOrNull(4)?.trim() ?: ""
            if (id.isNotEmpty()) name to id else null
        }

        // Parse api beaches
        val apiBeachesList = mutableListOf<Triple<String, String, String>>() // id, name, dgse
        val beachRegex = """\s*\{\s*"id"\s*:\s*(\d+)\s*,\s*"name"\s*:\s*"([^"]+)"\s*,\s*"dgse"\s*:\s*(?:null|([^,}]+))""".toRegex()
        beachRegex.findAll(beachesJson!!).forEach { match ->
            val id = match.groupValues.getOrNull(1) ?: ""
            val name = match.groupValues.getOrNull(2) ?: ""
            var dgse = match.groupValues.getOrNull(3) ?: ""
            dgse = dgse.replace("\"", "").trim()
            apiBeachesList.add(Triple(id, name, dgse))
        }

        println("Total CSV beaches: ${csvBeaches.size}")
        println("Total API beaches: ${apiBeachesList.size}")

        var matchedCount = 0
        val mismatches = mutableListOf<Pair<String, String>>()
        csvBeaches.forEach { (csvName, csvId) ->
            val match = apiBeachesList.find { it.third == csvId }
            if (match != null) {
                matchedCount++
            } else {
                mismatches.add(csvName to csvId)
            }
        }

        println("SUCCESSFULLY MATCHED: $matchedCount beaches out of ${csvBeaches.size}")
        println("MISMATCH COUNT: ${mismatches.size}")
        if (mismatches.isNotEmpty()) {
            println("First 15 mismatches: ${mismatches.take(15)}")
        }

        // Let's print out what some matching API beach dgse values look like to see if leading zeroes are present:
        val apiWithDgse = apiBeachesList.filter { it.third.isNotEmpty() }
        println("First 10 non-empty API dgse values: ${apiWithDgse.take(10)}")
    }
  }
}



