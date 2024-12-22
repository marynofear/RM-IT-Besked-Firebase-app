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
import java.util.LinkedList
import java.util.Queue

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "critical_it_alerts"
        private const val GROUP_KEY = "com.notificationapp.NOTIFICATIONS"
        private const val MAX_NOTIFICATIONS = 2
    }

    private val notificationQueue: Queue<NotificationInfo> = LinkedList()

    data class NotificationInfo(
        val id: Int,
        val title: String,
        val message: String
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FCM Service Created")
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "🔔 onMessageReceived triggered")

        try {
            if (remoteMessage.data.isNotEmpty()) {
                handleDataMessage(remoteMessage)
            } else {
                remoteMessage.notification?.let { notification ->
                    showNotification(
                        notification.title ?: "New Message",
                        notification.body ?: "No content"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
        }
    }

    private fun handleDataMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "New Message"
        val message = remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "No content"

        showNotification(title, message)
    }

    private fun showNotification(title: String, message: String) {
        try {
            val notificationId = System.currentTimeMillis().toInt()

            // Add new notification to queue
            notificationQueue.offer(NotificationInfo(notificationId, title, message))

            // Remove oldest if we exceed MAX_NOTIFICATIONS
            if (notificationQueue.size > MAX_NOTIFICATIONS) {
                val removedNotification = notificationQueue.poll()
                removedNotification?.let {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(it.id)
                }
            }

            // Show notifications for all items in queue
            notificationQueue.forEach { notificationInfo ->
                showSingleNotification(notificationInfo)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    private fun showSingleNotification(notificationInfo: NotificationInfo) {
        val intent = Intent(this, MessageDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", notificationInfo.title)
            putExtra("body", notificationInfo.message)
            putExtra("notification_id", notificationInfo.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationInfo.id,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationInfo.title)
            .setContentText(notificationInfo.message)
            .setAutoCancel(true)
            .setOngoing(true)
            .setGroup(GROUP_KEY)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationInfo.message))
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationInfo.id, notification)
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