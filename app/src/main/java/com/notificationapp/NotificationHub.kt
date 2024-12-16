package com.notificationapp

import android.content.Context  // Changed from Activity import
import android.util.Log
import com.microsoft.windowsazure.messaging.NotificationHub as AzureNotificationHub

class NotificationHub(context: Context) {  // Changed from Activity to Context
    companion object {
        private const val TAG = "NotificationHub"
        private const val FCM_V1_TAG = "fcmv1"
    }

    private val hub: AzureNotificationHub

    init {
        try {
            Log.d(TAG, "Initializing Azure Notification Hub...")

            hub = AzureNotificationHub(
                NotificationSettings.HUB_NAME,
                NotificationSettings.HUB_LISTEN_CONNECTION_STRING,
                context.applicationContext  // This remains the same
            )

            Log.d(TAG, "Azure Notification Hub initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Azure Notification Hub", e)
            throw e
        }
    }

    fun registerWithNotificationHubs(fcmToken: String) {
        Thread {
            try {
                Log.d(TAG, "Registering with Azure Notification Hub...")
                Log.d(TAG, "FCM Token: ${fcmToken.take(10)}...")

                val registrationId = hub.register(fcmToken, FCM_V1_TAG)
                Log.d(TAG, "Registration successful. ID: $registrationId")

            } catch (e: Exception) {
                Log.e(TAG, "Registration failed", e)
            }
        }.start()
    }
}