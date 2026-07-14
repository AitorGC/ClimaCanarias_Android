package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.repository.WeatherRepository
import com.example.security.AemetCredentialManager

class AemetWarningWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AemetWarningWorker", "Background work started to check AEMET warnings.")
        
        val apiKey = AemetCredentialManager.getAemetApiKey()
        if (apiKey.isBlank()) {
            Log.w("AemetWarningWorker", "AEMET API key is not configured. Skipping background check.")
            return Result.success()
        }

        val sharedPrefs = applicationContext.getSharedPreferences("clima_canarias_prefs", Context.MODE_PRIVATE)
        val selectedIslands = sharedPrefs.getStringSet("selected_islands", emptySet()) ?: emptySet()
        
        if (selectedIslands.isEmpty()) {
            Log.d("AemetWarningWorker", "No preferred islands selected for alerts. Skipping check.")
            return Result.success()
        }

        try {
            val repository = WeatherRepository(applicationContext)
            val warningsList = repository.fetchAemetWarnings(apiKey)
            
            if (warningsList.isEmpty()) {
                Log.d("AemetWarningWorker", "No warnings returned from AEMET API.")
                return Result.success()
            }

            // Filter warnings that match selected islands
            val relevantWarnings = warningsList.filter { warning ->
                val ambito = warning.ambitoGeografico?.lowercase() ?: ""
                selectedIslands.any { island ->
                    ambito.contains(island.lowercase())
                }
            }

            if (relevantWarnings.isNotEmpty()) {
                val notifiedIds = sharedPrefs.getStringSet("notified_warnings", emptySet()) ?: emptySet()
                val newNotifiedIds = notifiedIds.toMutableSet()
                var hasNew = false

                for (warning in relevantWarnings) {
                    val warningId = "${warning.fenomeno}_${warning.ambitoGeografico}_${warning.nivel}_${warning.fechaInicio}"
                    if (!notifiedIds.contains(warningId)) {
                        sendLocalNotification(warning)
                        newNotifiedIds.add(warningId)
                        hasNew = true
                    }
                }

                if (hasNew) {
                    sharedPrefs.edit().putStringSet("notified_warnings", newNotifiedIds).apply()
                }
            } else {
                Log.d("AemetWarningWorker", "No warnings match user selected islands: $selectedIslands")
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("AemetWarningWorker", "Error in AEMET background warnings check", e)
            return Result.retry()
        }
    }

    private fun sendLocalNotification(warning: com.example.data.AemetWarningDomainData) {
        val channelId = "aemet_alerts_channel"
        val channelName = "Alertas Meteorológicas Canarias"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Canal de avisos en tiempo real para las Islas Canarias"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val levelText = warning.nivel?.uppercase() ?: "AVISO"
        val title = "⚠️ Aviso $levelText: ${warning.fenomeno ?: "Fenómeno Adverso"}"
        val text = "${warning.ambitoGeografico ?: "Canarias"}: ${warning.descripcion ?: "Se ha emitido un aviso."}"

        val intent = Intent(applicationContext, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, flags)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val warningHash = (warning.fenomeno?.hashCode() ?: 0) + (warning.ambitoGeografico?.hashCode() ?: 0)
        val notificationId = (warningHash + (System.currentTimeMillis().toInt() / 1000)).coerceAtLeast(1)
        notificationManager.notify(notificationId, builder.build())
        Log.d("AemetWarningWorker", "Sent local notification for: ${warning.fenomeno} in ${warning.ambitoGeografico}")
    }
}
