package com.example.purrfectpaircat

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.purrfectpaircat.api.AuthService
import com.example.purrfectpaircat.api.LoginRequest
import com.example.purrfectpaircat.api.LoginResponse
import com.example.purrfectpaircat.api.RetrofitClient
import com.example.purrfectpaircat.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authService = RetrofitClient.authService

        val passwordEditText = findViewById<EditText>(R.id.password)
        val showPasswordIcon = findViewById<ImageView>(R.id.showpassword)

        var isPasswordVisible = false

        showPasswordIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                // Show password
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                showPasswordIcon.setImageResource(R.drawable.ic_showpassword) // Change to show icon
            } else {
                // Hide password
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                showPasswordIcon.setImageResource(R.drawable.ic_hidepassword) // Change to hide icon
            }

            // Move cursor to the end to maintain user experience
            passwordEditText.setSelection(passwordEditText.text.length)
        }


        // Login Button Click
        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (email.contains(" ")) {
                    Toast.makeText(this, "Email cannot contain spaces", Toast.LENGTH_SHORT).show()
                } else if (!email.endsWith("@gmail.com")) {
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                } else {
                    loginUser(email, password)
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Register Link Click
        binding.tvHaventAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Back Button Click
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, WelcomeScreenActivity::class.java))
        }
    }


    private fun loginUser(email: String, password: String) {
        val request = LoginRequest(email, password)
        authService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && !loginResponse.error) {
                        // Save user data to SharedPreferences if login is successful
                        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("fullname", loginResponse.user?.fullname)
                        editor.putString("contact_number", loginResponse.user?.contactNumber)
                        editor.putString("facebook_name", loginResponse.user?.facebookName)
                        editor.putString("home_address", loginResponse.user?.homeAddress)
                        editor.putString("token", loginResponse.token)  // Save the token
                        editor.apply()

                        // Show welcome message
                        Toast.makeText(this@LoginActivity, "Welcome, ${loginResponse.user?.fullname}!", Toast.LENGTH_SHORT).show()

                        // Navigate to HomeActivity
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish() // Close login screen
                    } else {
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login failed!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}