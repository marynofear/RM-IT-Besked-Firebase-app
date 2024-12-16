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
        // Create channel at service creation
        createNotificationChannel()
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "🔔 onMessageReceived triggered")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Data payload: ${remoteMessage.data}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")

        // Extract notification data
        val notificationTitle = remoteMessage.notification?.title
        val notificationBody = remoteMessage.notification?.body

        if (notificationTitle != null && notificationBody != null) {
            showNotification(notificationTitle, notificationBody)
        }
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

    private fun showNotification(title: String, message: String) {
        Log.d(TAG, "Building notification for: $title")

        // Create Intent for MessageDetailActivity
        val intent = Intent(this, MessageDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_title", title)
            putExtra("notification_message", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown with ID: $notificationId")
    }
}