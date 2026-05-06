package com.example.budgetsphere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "LoginActivity started")

        // If user is already logged in, skip login screen
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.getString("username", null) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Sign In button
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate fields
            if (username.isEmpty()) {
                binding.etUsername.error = "Please enter your username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            // Check credentials against database
            lifecycleScope.launch {
                try {
                    val db   = AppDatabase.getInstance(applicationContext)
                    val user = db.userDao().login(username, password)

                    runOnUiThread {
                        if (user != null) {
                            Log.d(TAG, "Login success: $username")
                            // Save logged in user details
                            prefs.edit()
                                .putString("username", user.username)
                                .putString("fullName", user.fullName)
                                .apply()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.w(TAG, "Login failed: $username")
                            Toast.makeText(
                                this@LoginActivity,
                                "Incorrect username or password. Please try again.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login error: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Something went wrong. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Go to Register screen
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Google button — placeholder
        binding.btnGoogle.setOnClickListener {
            Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show()
        }

        // Facebook button — placeholder
        binding.btnFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook sign-in coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}