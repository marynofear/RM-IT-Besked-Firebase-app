package com.notificationapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "critical_it_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FCM Service Created")
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "🔔 onMessageReceived triggered")

        val notificationTitle = remoteMessage.notification?.title
        val notificationBody = remoteMessage.notification?.body

        if (notificationTitle != null && notificationBody != null) {
            showNotification(notificationTitle, notificationBody)
        }
    }

    private fun showNotification(title: String, message: String) {
        Log.d(TAG, "⭐ Building notification with title: $title")

        // Create main activity intent as base
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Create detail activity intent
        val detailIntent = Intent(this, MessageDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }

        // Create an array of intents for the synthetic back stack
        val intents = arrayOf(mainIntent, detailIntent)

        // Create pending intent with the synthetic back stack
        val pendingIntent = PendingIntent.getActivities(
            this,
            System.currentTimeMillis().toInt(),
            intents,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "⭐ Notification shown with ID: $notificationId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Critical IT Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical system notifications from IT department"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: ${token.take(10)}...")
        try {
            NotificationHub(applicationContext).registerWithNotificationHubs(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register new token with Azure", e)
        }
    }
}