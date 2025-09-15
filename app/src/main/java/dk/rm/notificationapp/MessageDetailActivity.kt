// app/src/main/java/dk/rm/notificationapp/MessageDetailActivity.kt 15.09.25 12:40
package dk.rm.notificationapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MessageDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_detail)

        val titleView: TextView = findViewById(R.id.titleTextView)
        val messageView: TextView = findViewById(R.id.messageTextView)

        titleView.text = intent?.getStringExtra("title") ?: "RM IT Besked"
        messageView.text = intent?.getStringExtra("body") ?: ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
        return true
    }
}
