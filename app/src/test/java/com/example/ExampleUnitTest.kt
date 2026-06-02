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



