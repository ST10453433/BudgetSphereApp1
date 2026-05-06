package com.example.budgetsphere

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.data.User
import com.example.budgetsphere.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "RegisterActivity started")

        // Create Account button
        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()
            val termsChecked = binding.cbTerms.isChecked

            // Validate every field
            if (fullName.isEmpty()) {
                binding.etFullName.error = "Please enter your full name"
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                binding.etUsername.error = "Please enter a username"
                return@setOnClickListener
            }
            if (username.length < 3) {
                binding.etUsername.error = "Username must be at least 3 characters"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter a password"
                return@setOnClickListener
            }
            if (password.length < 4) {
                binding.etPassword.error = "Password must be at least 4 characters"
                return@setOnClickListener
            }
            if (password != confirm) {
                binding.etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }
            if (!termsChecked) {
                Toast.makeText(
                    this,
                    "Please agree to the Terms of Service and Privacy Policy",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Save new user to database
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(applicationContext)

                    // Check if username already taken
                    val existing = db.userDao().findByUsername(username)
                    if (existing != null) {
                        runOnUiThread {
                            binding.etUsername.error = "Username already taken — choose another"
                        }
                        return@launch
                    }

                    // Insert the new user
                    db.userDao().insert(
                        User(
                            fullName = fullName,
                            username = username,
                            password = password
                        )
                    )
                    Log.d(TAG, "Registered: $username ($fullName)")

                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish() // Go back to login
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Register error: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Back to login
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
}