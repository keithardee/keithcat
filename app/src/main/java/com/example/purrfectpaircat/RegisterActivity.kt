package com.example.purrfectpaircat
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.purrfectpaircat.api.RegisterRequest
import com.example.purrfectpaircat.api.RegisterResponse
import com.example.purrfectpaircat.api.RetrofitClient
import com.example.purrfectpaircat.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// Emoji filter to prevent emoji input
class EmojiInputFilter(private val context: AppCompatActivity) : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        if (source != null) {
            for (i in start until end) {
                val type = Character.getType(source[i])
                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    Toast.makeText(context, "Emojis are not allowed!", Toast.LENGTH_SHORT).show()
                    return "" // Block emoji
                }
            }
        }
        return null // Accept input
    }
}

class ContactNumberInputFilter : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // Prevent non-numeric input
        if (source != null && !source.toString().matches(Regex("[0-9]+"))) {
            return "" // Block input if not a number
        }
        return null
    }
}


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set input filters to prevent emojis
        val emojiFilter = arrayOf(EmojiInputFilter(this))
        binding.fullname.filters = emojiFilter
        binding.email.filters = emojiFilter
        binding.fbName.filters = emojiFilter
        binding.homeAddress.filters = emojiFilter
        binding.contactNumber.filters = emojiFilter

        // Set input properties
        binding.fullname.setSingleLine(true)
        binding.email.setSingleLine(true)
        binding.fbName.setSingleLine(true)
        binding.homeAddress.setSingleLine(true)
        binding.contactNumber.setSingleLine(true)
        binding.contactNumber.filters = arrayOf(ContactNumberInputFilter(), InputFilter.LengthFilter(11))

        // Enforce "09" prefix
        binding.contactNumber.setText("09")
        binding.contactNumber.setSelection(2) // Move cursor after "09"

        binding.contactNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.toString().startsWith("09")) {
                    binding.contactNumber.setText("09")
                    binding.contactNumber.setSelection(2) // Keep cursor after "09"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        binding.password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.confirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // SHOW PASSWORD AND HIDE PASSWORD
        var isPasswordVisible = false

        binding.showpassword.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                binding.password.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                binding.showpassword.setImageResource(R.drawable.ic_hidepassword) // Use hide icon
            } else {
                // Show password
                binding.password.transformationMethod = null
                binding.showpassword.setImageResource(R.drawable.ic_showpassword) // Use show icon
            }
            // Keep cursor at the end
            binding.password.setSelection(binding.password.text.length)

            isPasswordVisible = !isPasswordVisible
        }

        // Navigation
        binding.tvHaveAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(this, WelcomeScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        // Registration Button Click
        binding.btnRegister.setOnClickListener {
            val fullname = binding.fullname.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val contactNumber = binding.contactNumber.text.toString().trim()
            val facebookName = binding.fbName.text.toString().trim()
            val homeAddress = binding.homeAddress.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            // Validations
            if (fullname.isEmpty() || email.isEmpty() || contactNumber.isEmpty() || facebookName.isEmpty() ||
                homeAddress.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter all required information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.contains(" ")) {
                Toast.makeText(this, "Email cannot contain spaces", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (contactNumber.length != 11) {
                Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create request
            val request = RegisterRequest(fullname, email, contactNumber, facebookName, homeAddress, password)

            // Debugging Logs
            Log.d("RegisterActivity", "Sending Registration Request: $request")

            // API Call
            RetrofitClient.authService.register(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    Log.d("API_RESPONSE", "Response Code: ${response.code()}")

                    if (response.isSuccessful) {
                        val res = response.body()

                        if (res != null && !res.error) {
                            // Save the user data in SharedPreferences
                            val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()

                            // Save relevant user data to SharedPreferences
                            editor.putString("fullname", fullname)
                            editor.putString("email", email)
                            editor.putString("contact_number", contactNumber)
                            editor.putString("facebook_name", facebookName)
                            editor.putString("home_address", homeAddress)
                            editor.putString("password", password) // Optionally save password (though usually, you'd avoid this for security reasons)
                            editor.apply()

                            // Navigate to LoginActivity after successful registration
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish() // Close RegisterActivity
                        } else {
                            Toast.makeText(this@RegisterActivity, "Server returned an empty response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Handle HTTP error responses (4xx, 5xx)
                        val errorBody = response.errorBody()?.string()
                        Log.e("API_ERROR", "Server Error: ${response.code()} - $errorBody")

                        val errorMessage = when (response.code()) {
                            400 -> "Bad Request - Check input fields."
                            401 -> "Unauthorized - Invalid credentials."
                            404 -> "Not Found - Check API URL."
                            500 -> "Server Error - Try again later."
                            else -> "Unexpected Error: ${response.code()}"
                        }

                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Log.e("RegisterFailure", "Network error: ${t.message}")
                    Toast.makeText(this@RegisterActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
