package com.siwanonts.bioauthpoctest

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetPinActivity : AppCompatActivity(), View.OnClickListener {

    private val enteredPin = StringBuilder()
    private var firstPin = ""
    private var isConfirmingPin = false

    private lateinit var pinDots: List<View>
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_pin)
        supportActionBar?.title = "Create a New PIN"

        titleTextView = findViewById(R.id.text_title)
        initializePinDots()
        setupClickListeners()
    }

    private fun initializePinDots() {
        pinDots = listOf(
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2),
            findViewById(R.id.dot_3),
            findViewById(R.id.dot_4),
            findViewById(R.id.dot_5),
            findViewById(R.id.dot_6)
        )
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btn_0).setOnClickListener(this)
        findViewById<Button>(R.id.btn_1).setOnClickListener(this)
        findViewById<Button>(R.id.btn_2).setOnClickListener(this)
        findViewById<Button>(R.id.btn_3).setOnClickListener(this)
        findViewById<Button>(R.id.btn_4).setOnClickListener(this)
        findViewById<Button>(R.id.btn_5).setOnClickListener(this)
        findViewById<Button>(R.id.btn_6).setOnClickListener(this)
        findViewById<Button>(R.id.btn_7).setOnClickListener(this)
        findViewById<Button>(R.id.btn_8).setOnClickListener(this)
        findViewById<Button>(R.id.btn_9).setOnClickListener(this)
        findViewById<ImageButton>(R.id.btn_backspace).setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_backspace -> {
                if (enteredPin.isNotEmpty()) {
                    enteredPin.deleteCharAt(enteredPin.length - 1)
                }
            }
            else -> {
                if (enteredPin.length < 6) {
                    val button = view as Button
                    enteredPin.append(button.text)
                }
            }
        }
        updatePinDots()
        checkPinComplete()
    }

    private fun updatePinDots() {
        for (i in pinDots.indices) {
            if (i < enteredPin.length) {
                pinDots[i].setBackgroundResource(R.drawable.pin_dot_filled)
            } else {
                pinDots[i].setBackgroundResource(R.drawable.pin_dot_empty)
            }
        }
    }

    private fun checkPinComplete() {
        if (enteredPin.length < 6) return // Do nothing if PIN is not 6 digits yet

        if (!isConfirmingPin) {
            // --- First Stage: User has entered the first PIN ---
            firstPin = enteredPin.toString()
            isConfirmingPin = true

            // Reset for confirmation stage
            titleTextView.text = "Confirm your PIN"
            enteredPin.clear()
            // Add a small delay so the user sees the last dot fill before it clears
            pinDots.forEach { it.postDelayed({ updatePinDots() }, 100) }

        } else {
            // --- Second Stage: User has entered the confirmation PIN ---
            val secondPin = enteredPin.toString()
            if (firstPin == secondPin) {
                // --- Success: PINs match ---
                KeyStorePinManager.savePin(this, firstPin)
                Toast.makeText(this, "PIN created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // --- Failure: PINs do not match ---
                Toast.makeText(this, "PINs do not match. Please start over.", Toast.LENGTH_LONG).show()

                // Reset to the very beginning
                firstPin = ""
                isConfirmingPin = false
                titleTextView.text = "Create a new PIN"
                enteredPin.clear()
                pinDots.forEach { it.postDelayed({ updatePinDots() }, 100) }
            }
        }
    }
}