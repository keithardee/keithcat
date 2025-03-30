package com.example.purrfectpaircat

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.purrfectpaircat.databinding.ActivityChangepassBinding
import com.example.purrfectpaircat.databinding.ActivityLoginBinding

class ChangepassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangepassBinding

    lateinit var oldpassword : EditText
    lateinit var newpassword : EditText
    lateinit var confirmnewpassword : EditText
    lateinit var confirm : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChangepassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backButton = findViewById<ImageButton>(R.id.backButton)


        // BACK BUTTON
        backButton.setOnClickListener {
            finish() // This will close ChangepassActivity and return to the previous activity that contains your SettingsFragment
        }

        // Show/Hide Password for New Password Field
        var isNewPasswordVisible = false
        binding.showNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible

            if (isNewPasswordVisible) {
                binding.newPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.showNewPassword.setImageResource(R.drawable.ic_showpassword) // Update with your show icon
            } else {
                binding.newPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.showNewPassword.setImageResource(R.drawable.ic_hidepassword) // Update with your hide icon
            }
            binding.newPassword.setSelection(binding.newPassword.text.length)
        }



        binding.btnConfirm.setOnClickListener {
            val oldPassword = binding.oldPassword.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()
            val confirmNewPassword = binding.confirmNewPassword.text.toString().trim()

            if (oldPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()) {
                // Check if newPassword and confirmNewPassword match
                if (newPassword != confirmNewPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                // Check if newPassword and confirmNewPassword are at least 8 characters long
                else if (newPassword.length < 8 || confirmNewPassword.length < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Password update successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Please enter all password fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}