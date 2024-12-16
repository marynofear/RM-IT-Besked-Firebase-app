package com.notificationapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var notificationHub: NotificationHub

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
            initializeFCM()
        } else {
            Toast.makeText(this, "FCM requires notification permission", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate - Intent action: ${intent?.action}")
        Log.d(TAG, "onCreate - Intent flags: ${intent?.flags}")
        Log.d(TAG, "onCreate - Intent extras: ${intent?.extras}")

        notificationHub = NotificationHub(this)
        checkNotificationPermission()

        // Handle notification click
        if (intent?.action == "NOTIFICATION_CLICK") {
            Log.d(TAG, "onCreate - Processing notification click")
            handleNotificationData(intent)
        } else {
            Log.d(TAG, "onCreate - Regular app launch")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle notification click when app is in background
        if (intent?.action == "NOTIFICATION_CLICK") {
            handleNotificationData(intent)
        }
    }

    private fun handleNotificationData(intent: Intent) {
        Log.d(TAG, "=== Handle Notification Data Started ===")
        Log.d(TAG, "Intent action: ${intent.action}")

        val title = intent.getStringExtra("notification_title")
        val message = intent.getStringExtra("notification_message")

        Log.d(TAG, "Extracted title: $title")
        Log.d(TAG, "Extracted message: $message")

        if (title != null && message != null) {
            Log.d(TAG, "Starting MessageDetailActivity")
            startActivity(Intent(this, MessageDetailActivity::class.java).apply {
                putExtra("notification_title", title)
                putExtra("notification_message", message)
            })
        }
    }


    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                    initializeFCM()
                }
                else -> {
                    Log.d(TAG, "Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            initializeFCM()
        }
    }

    private fun initializeFCM() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                task.result?.let { token ->
                    Log.d(TAG, "FCM token received: ${token.take(10)}...")
                    notificationHub.registerWithNotificationHubs(token)
                }
            }
    }
}