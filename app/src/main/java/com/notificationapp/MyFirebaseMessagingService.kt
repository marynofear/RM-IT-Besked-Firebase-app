package com.notificationapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FCMService"
    private val CHANNEL_ID = "critical_it_alerts"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FCM Service Created")
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NotificationApp:FCMWakeLock"
        )

        try {
            wakeLock.acquire(10*60*1000L)

            Log.d(TAG, "Message received from: ${remoteMessage.from}")
            Log.d(TAG, "Full message data: ${remoteMessage.data}")
            Log.d(TAG, "Message ID: ${remoteMessage.messageId}")  // Added this line

            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "Message Notification Title: ${notification.title}")
                Log.d(TAG, "Message Notification Body: ${notification.body}")
                showCustomNotification(notification.title ?: "", notification.body ?: "")
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Critical IT Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Critical system notifications from IT department"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            importance = NotificationManager.IMPORTANCE_HIGH
            setBypassDnd(true)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showCustomNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⚠️ IT Alert: $title")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .build()

        notificationManager.notify(notificationId, notification)

        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.notify(notificationId, notification)
        }, 2000)
    }
}