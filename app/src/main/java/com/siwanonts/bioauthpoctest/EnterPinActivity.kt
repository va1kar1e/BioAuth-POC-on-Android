package com.siwanonts.bioauthpoctest

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EnterPinActivity : AppCompatActivity(), View.OnClickListener {

    private val enteredPin = StringBuilder()
    private lateinit var pinDots: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_pin)
        supportActionBar?.title = "Enter Your PIN"

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
        // Set this activity as the click listener for all number buttons
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
        if (enteredPin.length == 6) {
            if (KeyStorePinManager.checkPin(this, enteredPin.toString())) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                // Reset PIN after a short delay for user feedback
                enteredPin.clear()
                pinDots.forEach { it.postDelayed({ updatePinDots() }, 200) }
            }
        }
    }
}