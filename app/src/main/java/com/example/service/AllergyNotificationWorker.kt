package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.AllergenEvaluator
import com.example.data.AllergenSeverity
import com.example.repository.SettingsManager
import com.example.repository.WeatherRepository
import java.util.concurrent.TimeUnit

/**
 * Background worker for checking allergen and aerobiological warnings
 * and dispatching local notifications when user-selected allergens exceed risk thresholds.
 * (Architecture Option D support)
 */
class AllergyNotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val settingsManager = SettingsManager(appContext)
            val allergySettings = settingsManager.settings.value

            // If user hasn't selected any allergen, skip execution
            val hasAnyAllergen = allergySettings.allergyGrass ||
                    allergySettings.allergyOlive ||
                    allergySettings.allergyMugwort ||
                    allergySettings.allergyAlder ||
                    allergySettings.allergyBirch ||
                    allergySettings.allergyRagweed ||
                    allergySettings.sensitiveToDust

            if (!hasAnyAllergen) {
                return Result.success()
            }

            val repository = WeatherRepository(appContext)
            // Fetch weather/air quality data for Canarias reference coordinates
            val weatherData = repository.fetchWeather("Canarias", 28.2915, -16.6291)
            val airQuality = weatherData.airQuality

            if (airQuality != null) {
                val alerts = AllergenEvaluator.evaluateAlerts(airQuality, allergySettings)
                val highPriorityAlerts = alerts.filter {
                    it.severity == AllergenSeverity.HIGH || it.severity == AllergenSeverity.VERY_HIGH
                }

                if (highPriorityAlerts.isNotEmpty()) {
                    showAllergyNotification(highPriorityAlerts.first().title, highPriorityAlerts.first().alertDescription)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showAllergyNotification(title: String, body: String) {
        val channelId = "allergy_alerts_channel"
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Avisos de Alérgenos y Salud",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de concentraciones elevadas de polen o calima según tus alérgenos configurados"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val WORK_NAME = "AllergyAlertPeriodicWork"
        private const val NOTIFICATION_ID = 2001

        /**
         * Schedules or updates the periodic allergen check worker.
         * Runs periodically (e.g., every 6 hours) with network constraints.
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<AllergyNotificationWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Cancels periodic allergy alerts background worker.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
