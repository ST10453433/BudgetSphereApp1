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
        Log.d(TAG, "LoginActivity created")

        binding.btnSignIn.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) {
                binding.etUsername.error = "Enter username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Enter password"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db   = AppDatabase.getInstance(applicationContext)
                val user = db.userDao().login(username, password)
                if (user != null) {
                    Log.d(TAG, "Login successful: $username")
                    getSharedPreferences("prefs", MODE_PRIVATE)
                        .edit().putString("username", username).apply()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "Login failed: $username")
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid username or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // tvRegisterHere matches the ID in activity_login.xml
        binding.tvRegisterHere.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}