package com.example.kidmathlocker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MathLockActivity : AppCompatActivity() {

    private var targetPackage: String? = null
    private var num1: Int = 0
    private var num2: Int = 0
    private var correctAnswer: Int = 0

    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        setContentView(R.layout.activity_math_lock)

        // Rebind views to the new orientation layout
        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val answerEditText = findViewById<EditText>(R.id.answerEditText)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitAnswer)

        questionTextView.text = "$num1 + $num2 = ?"

        btnSubmit.setOnClickListener {
            val userText = answerEditText.text.toString().trim()
            if (userText.isNotEmpty()) {
                val userVal = userText.toIntOrNull()
                if (userVal == correctAnswer) {
                    grantSessionAndResumeGame()
                } else {
                    Toast.makeText(this, "Oops! Try again.", Toast.LENGTH_SHORT).show()
                    answerEditText.text.clear()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_lock)

        targetPackage = intent.getStringExtra("TARGET_PACKAGE")

        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitAnswer)

        if (savedInstanceState != null) {
            num1 = savedInstanceState.getInt("KEY_NUM1")
            num2 = savedInstanceState.getInt("KEY_NUM2")
            correctAnswer = savedInstanceState.getInt("KEY_ANSWER")
        } else {
            num1 = Random.nextInt(12, 60)
            num2 = Random.nextInt(11, 40)
            correctAnswer = num1 + num2
        }

        questionTextView.text = "$num1 + $num2 = ?"

        btnSubmit.setOnClickListener {
            val userText = answerEditText.text.toString().trim()
            if (userText.isNotEmpty()) {
                val userVal = userText.toIntOrNull()
                if (userVal == correctAnswer) {
                    grantSessionAndResumeGame()
                } else {
                    Toast.makeText(this, "Oops! Try again.", Toast.LENGTH_SHORT).show()
                    answerEditText.text.clear()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("KEY_NUM1", num1)
        outState.putInt("KEY_NUM2", num2)
        outState.putInt("KEY_ANSWER", correctAnswer)
    }

    private fun grantSessionAndResumeGame() {
        val pkg = targetPackage
        if (pkg != null) {
            val prefs = getSharedPreferences("LockerSession", Context.MODE_PRIVATE)

            // Read the custom duration set by the parent (default: 15 minutes)
            val durationMinutes = prefs.getInt("session_duration_minutes", 15)
            val sessionMillis = durationMinutes * 60 * 1000L
            val expiryTime = System.currentTimeMillis() + sessionMillis

            prefs.edit().putLong("session_expiry_$pkg", expiryTime).apply()

            Toast.makeText(this, "Correct! $durationMinutes minute(s) granted.", Toast.LENGTH_SHORT).show()

            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(launchIntent)
            }
        }
        finish()
    }

    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }
}