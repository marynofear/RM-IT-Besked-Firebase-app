//MessageDetailActivity.kt
package com.notificationapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MessageDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get data from intent
        val title = intent.getStringExtra("notification_title") ?: "No Title"
        val message = intent.getStringExtra("notification_message") ?: "No Message"

        // Set the action bar title
        supportActionBar?.title = "Alert Details"

        // Display the data
        findViewById<TextView>(R.id.titleTextView).text = title
        findViewById<TextView>(R.id.messageTextView).text = message
    }

    // Handle the back button press using the new approach
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}