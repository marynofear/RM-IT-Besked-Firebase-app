// app/src/main/java/com/notificationapp/MyFirebaseMessagingService.kt 14.09.25 12:34
package com.notificationapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "critical_it_alerts"
        private const val GROUP_KEY = "com.notificationapp.NOTIFICATIONS"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: ${token.take(10)}...")
        BackendRegistration.register(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "🔔 onMessageReceived")

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "RM IT Besked"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "Ny besked"

        // Windows/Electron felter vi matcher:
        val notificationId = data["notificationId"] ?: data["id"] ?: System.currentTimeMillis().toString()
        val collapseKey = data["collapseKey"]
            ?: data["collapse_key"]
            ?: data["collapsekey"]
            ?: "general|${title.lowercase().replace("\\s+".toRegex(), "-").take(60)}"

        val priority = (data["priority"] ?: "high").lowercase()
        val ttlMs = data["ttlMs"]?.toLongOrNull()
            ?: data["ttlSeconds"]?.toLongOrNull()?.times(1000)
        val expiresAt = data["expiresAt"]?.toLongOrNull()

        // Drop hvis udløbet
        val now = System.currentTimeMillis()
        val sentAtMs = data["timestamp"]?.toLongOrNull()
            ?: data["timestamp"]?.let { runCatching { java.time.Instant.parse(it).toEpochMilli() }.getOrNull() }
            ?: now
        val computedExpiry = expiresAt ?: ttlMs?.let { sentAtMs + it }
        if (computedExpiry != null && now > computedExpiry) {
            Log.i(TAG, "⏭️ Dropping expired message id=$notificationId")
            return
        }

        // Bekræft levering (fire-and-forget)
        BackendRegistration.confirmDelivery(applicationContext, notificationId)

        showCollapsedNotification(
            title = title,
            message = body,
            collapseKey = collapseKey,
            nid = notificationId.hashCode() // brug stabil int
        )
    }

    private fun showCollapsedNotification(title: String, message: String, collapseKey: String, nid: Int) {
        val intent = Intent(this, MessageDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("title", title)
            putExtra("body", message)
        }
        val pi = PendingIntent.getActivity(
            this, nid, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val prio = when ((/*ikke brugt nu*/"high")) {
            "max" -> NotificationCompat.PRIORITY_MAX
            "high" -> NotificationCompat.PRIORITY_HIGH
            "normal" -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .setPriority(prio)
            .setContentIntent(pi)

        // Brug Androids "tag" til collapse – samme key erstatter tidligere
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(collapseKey, nid, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Critical IT Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical system notifications from IT department"
                enableLights(true)
                enableVibration(true)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(chan)
        }
    }
}
