// NotificationHub.kt
package com.notificationapp

import android.app.Activity
import android.util.Log
import com.microsoft.windowsazure.messaging.NotificationHub as AzureNotificationHub

class NotificationHub(activity: Activity) {
    private val tag = "NotificationHub"
    private lateinit var hub: AzureNotificationHub

    init {
        try {
            Log.d(tag, "Starting FCM v1 NH initialization...")

            hub = AzureNotificationHub(
                NotificationSettings.HUB_NAME,
                NotificationSettings.HUB_LISTEN_CONNECTION_STRING,
                activity.applicationContext
            )

            Log.d(tag, "✓ NotificationHub initialized")
        } catch (e: Exception) {
            Log.e(tag, "✕ Error initializing NotificationHub", e)
            e.printStackTrace()
        }
    }

    fun registerWithNotificationHubs(fcmToken: String) {
        Thread {
            try {
                Log.d(tag, "Starting FCM v1 registration process...")
                Log.d(tag, "Token: ${fcmToken.take(10)}...")

                // Create FCM v1 specific registration
                val registrationId = hub.register(
                    fcmToken,
                    "fcmv1" // Adding a tag to identify FCM v1 registrations
                )

                Log.d(tag, "✓ FCM v1 Registration completed")
                Log.d(tag, "✓ Registration ID: $registrationId")

            } catch (e: Exception) {
                Log.e(tag, "✕ Registration failed", e)
                e.printStackTrace()
            }
        }.start()
    }
}