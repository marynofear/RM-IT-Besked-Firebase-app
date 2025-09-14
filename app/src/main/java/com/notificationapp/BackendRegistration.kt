package com.notificationapp

// app/src/main/java/com/notificationapp/BackendRegistration.kt 14.09.25 12:34
package com.notificationapp

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

object BackendRegistration {
    private const val TAG = "BackendRegistration"

    fun register(context: Context, fcmToken: String, channels: List<String> = AppConfig.DEFAULT_CHANNELS) {
        val input = workDataOf(
            "token" to fcmToken,
            "deviceId" to AppConfig.deviceId(context),
            "deviceName" to AppConfig.deviceName(),
            "channels" to channels.joinToString(",")
        )

        val request = OneTimeWorkRequestBuilder<RegisterWorker>()
            .setInputData(input)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "register-device-${AppConfig.deviceId(context)}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun confirmDelivery(context: Context, notificationId: String) {
        val input = workDataOf("notificationId" to notificationId)
        val request = OneTimeWorkRequestBuilder<ConfirmWorker>()
            .setInputData(input)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}

class RegisterWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    companion object { private const val TAG = "RegisterWorker" }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val token = inputData.getString("token") ?: return@withContext Result.failure()
        val deviceId = inputData.getString("deviceId") ?: return@withContext Result.failure()
        val deviceName = inputData.getString("deviceName") ?: "Android"
        val channels = inputData.getString("channels")?.split(",")?.filter { it.isNotBlank() } ?: AppConfig.DEFAULT_CHANNELS

        val url = URL("${AppConfig.BACKEND_DEVICES_BASE}/register")
        val payload = JSONObject().apply {
            put("fcmToken", token)
            put("deviceId", deviceId)
            put("computerName", deviceName)
            put("channels", channels)
            put("platform", "android")
            put("osType", "Android")
            put("osVersion", Build.VERSION.RELEASE ?: "unknown")
            put("appVersion", AppConfig.APP_VERSION)
            put("retryAttempt", runAttemptCount)
        }.toString()

        Log.d(TAG, "POST /register attempt=$runAttemptCount  deviceId=$deviceId")

        try {
            (url.openConnection() as HttpURLConnection).run {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                outputStream.use { it.write(payload.toByteArray()) }
                val code = responseCode
                if (code in 200..299 || code == 409) {
                    Log.d(TAG, "✅ register ok (code=$code)")
                    return@withContext Result.success()
                }
                // 4xx → ikke retry
                if (code in 400..499) {
                    Log.e(TAG, "❌ register client error $code")
                    return@withContext Result.failure()
                }
                Log.w(TAG, "🔁 register server error $code → retry")
                return@withContext Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ register exception: ${e.message}")
            return@withContext Result.retry()
        }
    }
}

class ConfirmWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    companion object { private const val TAG = "ConfirmWorker" }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val id = inputData.getString("notificationId") ?: return@withContext Result.failure()
        // Bemærk: Windows bruger base.replace('/devices','') + /apiV2/notifications/{id}/confirm
        // Her inferer vi base og bygger confirm-url som Windows gør:
        val base = AppConfig.BACKEND_DEVICES_BASE.removeSuffix("/devices")
        val url = URL("$base/apiV2/notifications/$id/confirm")

        try {
            (url.openConnection() as HttpURLConnection).run {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = false
                val code = responseCode
                if (code in 200..299) {
                    Log.d(TAG, "✅ confirm ok for $id")
                    return@withContext Result.success()
                }
                if (code in 400..499) {
                    Log.w(TAG, "ℹ️ confirm client $code (no retry)")
                    return@withContext Result.failure()
                }
                Log.w(TAG, "🔁 confirm server $code")
                return@withContext Result.retry()
            }
        } catch (e: Exception) {
            Log.d(TAG, "confirm exception: ${e.message}")
            return@withContext Result.retry()
        }
    }
}