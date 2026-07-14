package com.example

import org.junit.Test
import com.example.repository.WeatherRepository
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class FetchAemetStationsTest {
    @Test
    fun testFetch() = runBlocking {
        try {
            val app = ApplicationProvider.getApplicationContext<Application>()
            val repo = WeatherRepository(app)
            val apiKey = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhaXRvcmdjODlAZ21haWwuY29tIiwianRpIjoiODIzZGJkODAtNTdjNC00NGJiLWFjMzItMDA2OWE3ZWU2MmJkIiwiaXNzIjoiQUVNRVQiLCJpYXQiOjE3ODQwMjc0MTIsInVzZXJJZCI6IjgyM2RiZDgwLTU3YzQtNDRiYi1hYzMyLTAwNjlhN2VlNjJiZCIsInJvbGUiOiIifQ.mlpv3KmnxAWacOIiYqrfM2VCxuzcOyA8s-ItTNwYroc"
            val stations = repo.fetchAemetStations(apiKey)
            println("Stations fetched: ${stations.size}")
        } catch (e: Exception) {
            println("Test crash: ${e.message}")
            e.printStackTrace()
        }
    }
}
