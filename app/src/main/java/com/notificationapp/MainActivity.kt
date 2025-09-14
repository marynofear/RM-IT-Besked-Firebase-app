// app/src/main/java/com/notificationapp/MainActivity.kt 14.09.25 12:34
package com.notificationapp

import android.Manifest
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

    private var fcmTokenRetryCount = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) initializeFCM() else {
            Toast.makeText(this, "Tillad notifikationer i indstillinger", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (granted) initializeFCM() else requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else initializeFCM()
    }

    private fun initializeFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "FCM token fetch failed", task.exception)
                handleFCMTokenError()
                return@addOnCompleteListener
            }
            val token = task.result ?: return@addOnCompleteListener handleFCMTokenError()
            Log.d(TAG, "FCM token: ${token.take(10)}...")
            fcmTokenRetryCount = 0
            BackendRegistration.register(applicationContext, token)
        }
    }

    private fun handleFCMTokenError() {
        if (fcmTokenRetryCount < MAX_RETRIES) {
            fcmTokenRetryCount++
            window.decorView.postDelayed({ initializeFCM() }, 1000L * fcmTokenRetryCount)
        } else {
            Toast.makeText(this, "Kunne ikke initialisere FCM", Toast.LENGTH_LONG).show()
        }
    }
}