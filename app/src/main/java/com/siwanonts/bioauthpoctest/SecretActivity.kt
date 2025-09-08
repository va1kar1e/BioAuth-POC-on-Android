package com.siwanonts.bioauthpoctest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SecretActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret)

        // Find the logout button from the layout by its ID
        val logoutButton: Button = findViewById(R.id.btn_logout)

        // Set a listener to perform an action when the button is clicked
        logoutButton.setOnClickListener {
            // Create an Intent to go back to MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // Add flags to clear the activity stack. This prevents the user
            // from pressing the back button and returning to the SecretActivity
            // after logging out. It creates a fresh start.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            // Close the current SecretActivity
            finish()
        }
    }
}