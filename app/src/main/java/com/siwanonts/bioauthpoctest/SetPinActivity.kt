package com.siwanonts.bioauthpoctest


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetPinActivity : AppCompatActivity(), View.OnClickListener {

    // Variables to manage PIN entry state
    private val enteredPin = StringBuilder()
    private var firstPin = ""
    private var isConfirmingPin = false

    // UI elements
    private lateinit var pinDots: List<View>
    private lateinit var titleTextView: TextView

    // ✅ Map to store the randomized number for each button ID
    private val keyPadMapping = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_pin)
        supportActionBar?.title = "Create a New PIN"

        titleTextView = findViewById(R.id.text_title)
        initializePinDots()
        setupClickListeners()
        randomizeKeypad() // ✅ Shuffle the keypad on creation
    }

    private fun initializePinDots() {
        pinDots = listOf(
            findViewById(R.id.dot_1), findViewById(R.id.dot_2),
            findViewById(R.id.dot_3), findViewById(R.id.dot_4),
            findViewById(R.id.dot_5), findViewById(R.id.dot_6)
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

    /**
     * ✅ New function to randomize the keypad
     */
    private fun randomizeKeypad() {
        val numberButtons = listOf<Button>(
            findViewById(R.id.btn_0), findViewById(R.id.btn_1),
            findViewById(R.id.btn_2), findViewById(R.id.btn_3),
            findViewById(R.id.btn_4), findViewById(R.id.btn_5),
            findViewById(R.id.btn_6), findViewById(R.id.btn_7),
            findViewById(R.id.btn_8), findViewById(R.id.btn_9)
        )
        val numbers = (0..9).map { it.toString() }.shuffled()

        numberButtons.forEachIndexed { index, button ->
            val number = numbers[index]
            button.text = number
            keyPadMapping[button.id] = number
        }
    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_backspace) {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
            }
        } else {
            if (enteredPin.length < 6) {
                val number = keyPadMapping[view.id]
                enteredPin.append(number)
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

    @SuppressLint("SetTextI18n")
    private fun checkPinComplete() {
        if (enteredPin.length < 6) return // Exit if PIN is not 6 digits yet

        if (!isConfirmingPin) {
            firstPin = enteredPin.toString()
            isConfirmingPin = true

            titleTextView.text = "Confirm your PIN"
            enteredPin.clear()

            pinDots.forEach { it.postDelayed({ updatePinDots() }, 100) }

        } else {
            val secondPin = enteredPin.toString()
            if (firstPin == secondPin) {
                KeyStorePinManager.savePin(this, firstPin)
                Toast.makeText(this, "PIN created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Failure: PINs do not match
                Toast.makeText(this, "PINs do not match. Please start over.", Toast.LENGTH_LONG).show()

                // Reset everything to the very beginning
                firstPin = ""
                isConfirmingPin = false
                titleTextView.text = "Create a new PIN"
                enteredPin.clear()
                pinDots.forEach { it.postDelayed({ updatePinDots() }, 100) }
            }
        }
    }
}