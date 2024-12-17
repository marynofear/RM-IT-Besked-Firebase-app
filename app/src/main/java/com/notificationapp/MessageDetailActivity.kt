package com.notificationapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MessageDetailActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MessageDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        Log.d(TAG, "⭐ onCreate - Started")
        Log.d(TAG, "⭐ Intent extras: ${intent?.extras}")

        val title = intent?.getStringExtra("notification_title")
        val message = intent?.getStringExtra("notification_message")

        Log.d(TAG, "⭐ Received - Title: $title, Message: $message")

        findViewById<TextView>(R.id.titleTextView).text = title
        findViewById<TextView>(R.id.messageTextView).text = message

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Just finish this activity, let the system handle back stack
                finishAfterTransition()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        // Use finishAfterTransition for smoother navigation
        finishAfterTransition()
        return true
    }
}