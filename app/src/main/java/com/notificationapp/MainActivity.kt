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
        private const val MAX_RETRIES = 3
    }

    private lateinit var notificationHub: NotificationHub
    private var fcmTokenRetryCount = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            isGranted -> {
                Log.d(TAG, "Notification permission granted")
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
                initializeFCM()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Log.d(TAG, "Notification permission denied with 'Don't ask again'")
                Toast.makeText(this,
                    "Please enable notifications in app settings for full functionality",
                    Toast.LENGTH_LONG).show()
            }
            else -> {
                Log.d(TAG, "Notification permission denied")
                Toast.makeText(this, "FCM requires notification permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "⭐ onCreate - Started")
        logIntentDetails(intent)

        try {
            notificationHub = NotificationHub(this)
            handleIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing NotificationHub", e)
            Toast.makeText(this, "Error initializing notification system", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "⭐ onNewIntent called")
        logIntentDetails(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when {
            intent?.extras?.containsKey("title") == true -> {
                Log.d(TAG, "Handling notification intent")
                launchMessageDetail(intent)
            }
            intent?.action == Intent.ACTION_MAIN -> {
                Log.d(TAG, "Handling main action")
                checkNotificationPermission()
            }
            else -> {
                Log.d(TAG, "No specific action to handle")
                checkNotificationPermission()
            }
        }
    }

    private fun launchMessageDetail(intent: Intent) {
        try {
            val detailIntent = Intent(this, MessageDetailActivity::class.java).apply {
                putExtra("title", intent.getStringExtra("title"))
                putExtra("body", intent.getStringExtra("body"))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(detailIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching message detail", e)
        }
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
                    handleFCMTokenError()
                    return@addOnCompleteListener
                }

                task.result?.let { token ->
                    Log.d(TAG, "⭐ FCM token received: ${token.take(10)}...")
                    fcmTokenRetryCount = 0
                    notificationHub.registerWithNotificationHubs(token)
                }
            }
    }

    private fun handleFCMTokenError() {
        if (fcmTokenRetryCount < MAX_RETRIES) {
            fcmTokenRetryCount++
            Log.d(TAG, "Retrying FCM token fetch (Attempt $fcmTokenRetryCount)")
            android.os.Handler(mainLooper).postDelayed({
                initializeFCM()
            }, 1000L * fcmTokenRetryCount)
        } else {
            Log.e(TAG, "Failed to fetch FCM token after $MAX_RETRIES attempts")
            Toast.makeText(this,
                "Failed to initialize notification system. Please try again later.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "⭐ onDestroy called")
    }
}