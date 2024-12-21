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

        Log.d(TAG, "⭐ onCreate - Started")
        logIntentDetails(intent)

        notificationHub = NotificationHub(this)

        // Check if we were launched from a notification
        if (intent?.extras?.containsKey("title") == true) {
            // Launch MessageDetailActivity with the notification data
            val detailIntent = Intent(this, MessageDetailActivity::class.java).apply {
                putExtra("title", intent.getStringExtra("title"))
                putExtra("body", intent.getStringExtra("body"))
            }
            startActivity(detailIntent)
        } else if (intent?.action == Intent.ACTION_MAIN) {
            checkNotificationPermission()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "⭐ onNewIntent called")
        logIntentDetails(intent)
        setIntent(intent)
    }

    private fun logIntentDetails(intent: Intent?) {
        Log.d(TAG, "⭐ Intent details:")
        Log.d(TAG, "⭐ Action: ${intent?.action}")
        Log.d(TAG, "⭐ Flags: ${intent?.flags}")
        Log.d(TAG, "⭐ Categories: ${intent?.categories}")
        Log.d(TAG, "⭐ Extras: ${intent?.extras?.keySet()?.joinToString()}")

        intent?.extras?.keySet()?.forEach { key ->
            val value = intent.extras?.getString(key)
            Log.d(TAG, "⭐ Extra[$key]: $value")
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "⭐ Notification permission already granted")
                    initializeFCM()
                }
                else -> {
                    Log.d(TAG, "⭐ Requesting notification permission")
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
                    Log.e(TAG, "⭐ Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                task.result?.let { token ->
                    Log.d(TAG, "⭐ FCM token received: ${token.take(10)}...")
                    notificationHub.registerWithNotificationHubs(token)
                }
            }
    }
}