package com.siwanonts.bioauthpoctest

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EnterPinActivity : AppCompatActivity(), View.OnClickListener {

    private val enteredPin = StringBuilder()
    private lateinit var pinDots: List<View>

    private val keyPadMapping = mutableMapOf<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_pin)
        supportActionBar?.title = "Enter Your PIN"

        initializePinDots()
        setupClickListeners()
        randomizeKeypad()
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

    private fun randomizeKeypad() {
        // List of all number buttons
        val numberButtons = listOf<Button>(
            findViewById(R.id.btn_0), findViewById(R.id.btn_1),
            findViewById(R.id.btn_2), findViewById(R.id.btn_3),
            findViewById(R.id.btn_4), findViewById(R.id.btn_5),
            findViewById(R.id.btn_6), findViewById(R.id.btn_7),
            findViewById(R.id.btn_8), findViewById(R.id.btn_9)
        )

        // Create a shuffled list of digits 0-9
        val numbers = (0..9).map { it.toString() }.shuffled()

        // Assign shuffled numbers to buttons and store the mapping
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
        } else if (view is Button) {
            // It's a number button
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

    private fun checkPinComplete() {
        if (enteredPin.length == 6) {
            if (KeyStorePinManager.checkPin(this, enteredPin.toString())) {
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                enteredPin.clear()
                pinDots.forEach { it.postDelayed({ updatePinDots() }, 200) }
            }
        }
    }
}