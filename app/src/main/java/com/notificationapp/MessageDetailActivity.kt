package com.notificationapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MessageDetailActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MessageDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        Log.d(TAG, "⭐ onCreate - Started")
        Log.d(TAG, "Intent extras: ${intent?.extras?.keySet()?.joinToString()}")

        val titleView: TextView = findViewById(R.id.titleTextView)
        val messageView: TextView = findViewById(R.id.messageTextView)

        val title = intent?.getStringExtra("title") ?: "No Title Available"
        val message = intent?.getStringExtra("body") ?: "No Message Available"

        Log.d(TAG, "⭐ Setting title: $title")
        Log.d(TAG, "⭐ Setting message: $message")

        titleView.text = title
        messageView.text = message

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        window.decorView.invalidate()
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateUp()
        return true
    }

    private fun navigateUp() {
        val upIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(upIntent)
        finish()
    }
}