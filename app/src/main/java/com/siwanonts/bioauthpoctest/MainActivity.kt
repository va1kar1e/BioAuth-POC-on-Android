package com.siwanonts.bioauthpoctest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val pinLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // PIN login was successful, navigate to the secret page
            Toast.makeText(this, "PIN Accepted!", Toast.LENGTH_SHORT).show()
            goToSecretActivity()
        } else {
            // PIN login failed or was cancelled
            Toast.makeText(this, "PIN login cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAuthenticate: Button = findViewById(R.id.btn_authenticate)
        val btnUsePin: Button = findViewById(R.id.btn_use_pin) // ✅ Get the new button

        // --- Biometric Setup ---
        if (canAuthenticateWithBiometrics()) {
            setupBiometricPrompt()
            btnAuthenticate.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            btnAuthenticate.isEnabled = false // Disable if not supported
        }

        btnUsePin.setOnClickListener {
            // Check if a PIN has been created
            if (KeyStorePinManager.isPinSet(this)) {
                // If PIN exists, launch the Enter PIN screen
                val intent = Intent(this, EnterPinActivity::class.java)
                pinLoginLauncher.launch(intent)
            } else {
                // If no PIN exists, prompt the user to create one
                Toast.makeText(this, "Please set up a PIN first.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, SetPinActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun goToSecretActivity() {
        val intent = Intent(this, SecretActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity so the user can't go back to it
    }

    private fun canAuthenticateWithBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("BIOMETRIC_CHECK", "App can authenticate using biometrics.")
                return true
            }
            // ... other cases
            else -> return false
        }
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Biometric authentication succeeded!", Toast.LENGTH_SHORT).show()
                goToSecretActivity() // Use the helper function
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }

        biometricPrompt = BiometricPrompt(this, executor, callback)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }
}