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

        // btnCreateAccount matches ID in activity_register.xml
        binding.btnCreateAccount.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.etFullName.error = "Required"
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                binding.etUsername.error = "Required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Required"
                return@setOnClickListener
            }
            if (password.length < 4) {
                binding.etPassword.error = "Min 4 characters"
                return@setOnClickListener
            }
            if (password != confirm) {
                binding.etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }
            if (!binding.cbTerms.isChecked) {
                Toast.makeText(this, "Please accept the Terms of Service", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db       = AppDatabase.getInstance(applicationContext)
                val existing = db.userDao().findByUsername(username)
                if (existing != null) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Username already taken", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                db.userDao().insert(User(username = username, password = password))
                Log.d(TAG, "Registered: $username")
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // tvSignIn matches ID in activity_register.xml
        binding.tvSignIn.setOnClickListener { finish() }
    }
}