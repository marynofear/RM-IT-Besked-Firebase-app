package com.notificationapp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACKNOWLEDGE_ACTION") {
            val notificationId = intent.getIntExtra("notification_id", 0)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Cancel the notification
            notificationManager.cancel(notificationId)

            // Show confirmation to user
            Toast.makeText(context, "Alert acknowledged", Toast.LENGTH_SHORT).show()

            // You could also add logic here to:
            // - Log the acknowledgment
            // - Send confirmation to your backend
            // - Update UI if needed
        }
    }
}