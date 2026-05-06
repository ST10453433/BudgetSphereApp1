package com.example.budgetsphere

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.budgetsphere.databinding.ActivityMainBinding
import com.example.budgetsphere.ui.AddExpenseFragment
import com.example.budgetsphere.ui.BudgetGoalsFragment
import com.example.budgetsphere.ui.CategoryTotalsFragment
import com.example.budgetsphere.ui.DashboardFragment
import com.example.budgetsphere.ui.ExpenseListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check login
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.getString("username", null) == null) {
            Log.d(TAG, "Not logged in — going to LoginActivity")
            // FIX: class.java was corrupted by hyperlink
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "MainActivity created")

        // Start on Dashboard
        loadFragment(DashboardFragment())

        // FIX: bottomNav -> bottomNavigationView to match activity_main.xml
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // FIX: all R.id references were corrupted by hyperlinks
                R.id.nav_dashboard -> { loadFragment(DashboardFragment());        true }
                R.id.nav_add       -> { loadFragment(AddExpenseFragment());        true }
                R.id.nav_list      -> { loadFragment(ExpenseListFragment());       true }
                R.id.nav_totals    -> { loadFragment(CategoryTotalsFragment());    true }
                R.id.nav_goals     -> { loadFragment(BudgetGoalsFragment());       true }
                else -> false
            }
        }
    }

    // Called by DashboardFragment buttons to switch tabs
    fun navigateTo(navItemId: Int) {
        // FIX: bottomNav -> bottomNavigationView to match activity_main.xml
        binding.bottomNavigationView.selectedItemId = navItemId
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            // FIX: R.id was corrupted by hyperlink
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}