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

        // If already logged in, go straight to main
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.getString("username", null) != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Basic validation
            if (username.isEmpty()) {
                binding.etUsername.error = "Please enter your username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val db   = AppDatabase.getInstance(applicationContext)
                    val user = db.userDao().login(username, password)

                    runOnUiThread {
                        if (user != null) {
                            Log.d(TAG, "Login successful: $username")
                            // Save logged-in username for use across the app
                            prefs.edit().putString("username", user.username).apply()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Log.w(TAG, "Login failed for: $username")
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

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}