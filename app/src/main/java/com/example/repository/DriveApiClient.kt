package com.example.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class DriveApiClient(private val client: OkHttpClient) {

    suspend fun getAppConfigFileId(token: String): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name='weather_backup.json'")
            .addHeader("Authorization", "Bearer $token")
            .build()
            
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            val files = json.optJSONArray("files")
            if (files != null && files.length() > 0) {
                return@withContext files.getJSONObject(0).getString("id")
            }
        }
        null
    }

    suspend fun downloadAppConfig(token: String, fileId: String): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            .addHeader("Authorization", "Bearer $token")
            .build()
            
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            response.body?.string()
        }
    }

    suspend fun uploadAppConfig(token: String, fileId: String?, content: String): Boolean = withContext(Dispatchers.IO) {
        val metadata = JSONObject()
        metadata.put("name", "weather_backup.json")
        if (fileId == null) {
            metadata.put("parents", org.json.JSONArray().put("appDataFolder"))
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("metadata", null, metadata.toString().toRequestBody("application/json".toMediaType()))
            .addFormDataPart("file", "weather_backup.json", content.toRequestBody("application/json".toMediaType()))
            .build()

        val url = if (fileId == null) {
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
        } else {
            "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=multipart"
        }

        val method = if (fileId == null) "POST" else "PATCH"

        val request = Request.Builder()
            .url(url)
            .method(method, requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }
}
