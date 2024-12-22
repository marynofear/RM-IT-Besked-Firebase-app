package com.notificationapp

import android.content.Context
import android.util.Log
import com.microsoft.windowsazure.messaging.NotificationHub as AzureNotificationHub

class NotificationHub(context: Context) {
    companion object {
        private const val TAG = "NotificationHub"
        private const val FCM_V1_TAG = "fcmv1"
        private const val MAX_RETRIES = 3
    }

    private val hub: AzureNotificationHub

    init {
        try {
            Log.d(TAG, "Initializing Azure Notification Hub...")

            hub = AzureNotificationHub(
                NotificationSettings.HUB_NAME,
                NotificationSettings.HUB_LISTEN_CONNECTION_STRING,
                context.applicationContext
            )

            Log.d(TAG, "Azure Notification Hub initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Azure Notification Hub", e)
            throw e
        }
    }

    fun registerWithNotificationHubs(fcmToken: String) {
        Thread {
            var attempt = 0
            var success = false

            while (attempt < MAX_RETRIES && !success) {
                try {
                    Log.d(TAG, "Registering with Azure Notification Hub... Attempt ${attempt + 1}")
                    Log.d(TAG, "FCM Token: ${fcmToken.take(10)}...")

                    val registrationId = hub.register(fcmToken, FCM_V1_TAG)
                    Log.d(TAG, "Registration successful. ID: $registrationId")
                    success = true

                } catch (e: Exception) {
                    attempt++
                    Log.e(TAG, "Registration attempt $attempt failed", e)

                    if (attempt < MAX_RETRIES) {
                        val delayMs = 1000L * attempt  // Simple linear backoff
                        try {
                            Thread.sleep(delayMs)
                        } catch (ie: InterruptedException) {
                            Log.e(TAG, "Retry delay interrupted", ie)
                            break
                        }
                    }
                }
            }

            if (!success) {
                Log.e(TAG, "All registration attempts failed")
            }
        }.start()
    }
}