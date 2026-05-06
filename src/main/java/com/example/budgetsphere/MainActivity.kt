package com.example.budgetsphere

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.budgetsphere.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Guard: if not logged in, send to LoginActivity
        val prefs    = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)
        if (username.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Wire BottomNavigationView to NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        // Use NavigationUI directly instead of setupWithNavController extension
        // This avoids the missing extension function error
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
    }

    // Called by DashboardFragment buttons to switch tabs
    fun navigateTo(menuItemId: Int) {
        binding.bottomNavView.selectedItemId = menuItemId
    }
}