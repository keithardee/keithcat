package com.example.purrfectpaircat

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aboutus)

        val backButton = findViewById<ImageButton>(R.id.backButton)

        // BACK BUTTON: finish the activity to return to the previous screen
        backButton.setOnClickListener {
            finish()
        }
    }
}