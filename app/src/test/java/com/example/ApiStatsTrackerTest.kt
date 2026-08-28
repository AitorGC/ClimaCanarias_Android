package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApiCategory
import com.example.data.ApiStatsTracker
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApiStatsTrackerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        ApiStatsTracker.init(context)
        ApiStatsTracker.resetAllStats()
    }

    @Test
    fun testUrlClassification() {
        assertEquals(
            ApiCategory.OPEN_METEO_WEATHER,
            ApiCategory.fromUrl("https://api.open-meteo.com/v1/forecast?latitude=28.12&longitude=-15.43")
        )
        assertEquals(
            ApiCategory.OPEN_METEO_AQI,
            ApiCategory.fromUrl("https://air-quality-api.open-meteo.com/v1/air-quality?latitude=28.12&longitude=-15.43")
        )
        assertEquals(
            ApiCategory.OPEN_METEO_MARINE,
            ApiCategory.fromUrl("https://marine-api.open-meteo.com/v1/marine?latitude=28.12&longitude=-15.43")
        )
        assertEquals(
            ApiCategory.AEMET_WARNINGS,
            ApiCategory.fromUrl("https://opendata.aemet.es/opendata/api/avisos_cap/ultimo")
        )
        assertEquals(
            ApiCategory.AEMET_OPENDATA,
            ApiCategory.fromUrl("https://opendata.aemet.es/opendata/api/observacion/convencional/datos/estacion/C447A")
        )
        assertEquals(
            ApiCategory.IHM_TIDES,
            ApiCategory.fromUrl("http://ideihm.covam.es/api-ihm/getmarea?request=getlist&format=json")
        )
        assertEquals(
            ApiCategory.INFOPLAYAS,
            ApiCategory.fromUrl("https://www3.gobiernodecanarias.org/aplicaciones/infoplayas/socorrismo/api/beach")
        )
        assertEquals(
            ApiCategory.GOOGLE_DRIVE,
            ApiCategory.fromUrl("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder")
        )
    }

    @Test
    fun testRecordCallAndSummaryCalculations() {
        // Record 2 successful forecast calls and 1 failed call
        ApiStatsTracker.recordCall(
            url = "https://api.open-meteo.com/v1/forecast?latitude=28.12&longitude=-15.43",
            durationMs = 150,
            statusCode = 200,
            isSuccess = true
        )
        ApiStatsTracker.recordCall(
            url = "https://api.open-meteo.com/v1/forecast?latitude=28.12&longitude=-15.43",
            durationMs = 250,
            statusCode = 200,
            isSuccess = true
        )
        ApiStatsTracker.recordCall(
            url = "https://marine-api.open-meteo.com/v1/marine?latitude=28.12&longitude=-15.43",
            durationMs = 100,
            statusCode = 500,
            isSuccess = false,
            errorMsg = "HTTP 500"
        )

        val summary = ApiStatsTracker.statsFlow.value
        assertEquals(3L, summary.totalCalls)
        assertEquals(2L, summary.totalSuccess)
        assertEquals(1L, summary.totalFailed)
        assertEquals(66, summary.successRatePercentage)
        assertEquals((150 + 250 + 100) / 3L, summary.averageResponseTimeMs)

        val weatherStat = summary.statsList.find { it.categoryId == ApiCategory.OPEN_METEO_WEATHER.id }
        assertNotNull(weatherStat)
        assertEquals(2L, weatherStat!!.totalCalls)
        assertEquals(2L, weatherStat.successCalls)
        assertEquals(0L, weatherStat.failedCalls)
        assertEquals(200L, weatherStat.averageResponseTimeMs)

        val marineStat = summary.statsList.find { it.categoryId == ApiCategory.OPEN_METEO_MARINE.id }
        assertNotNull(marineStat)
        assertEquals(1L, marineStat!!.totalCalls)
        assertEquals(0L, marineStat.successCalls)
        assertEquals(1L, marineStat.failedCalls)
        assertEquals(500, marineStat.lastStatusCode)
        assertEquals("HTTP 500", marineStat.lastErrorMessage)
    }

    @Test
    fun testResetStats() {
        ApiStatsTracker.recordCall(
            url = "https://api.open-meteo.com/v1/forecast",
            durationMs = 200,
            statusCode = 200,
            isSuccess = true
        )
        assertEquals(1L, ApiStatsTracker.statsFlow.value.totalCalls)

        ApiStatsTracker.resetAllStats()
        assertEquals(0L, ApiStatsTracker.statsFlow.value.totalCalls)
        assertEquals(0L, ApiStatsTracker.statsFlow.value.totalSuccess)
        assertEquals(0L, ApiStatsTracker.statsFlow.value.totalFailed)
    }
}
