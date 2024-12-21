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

        val titleView: TextView = findViewById(R.id.titleTextView)
        val messageView: TextView = findViewById(R.id.messageTextView)

        // Update to use the correct keys from the intent
        val title = intent?.getStringExtra("title") ?: "Default Title"
        val message = intent?.getStringExtra("body") ?: "Default Message"

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