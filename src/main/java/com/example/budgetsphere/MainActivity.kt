package com.example.budgetsphere



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.budgetsphere.databinding.ActivityMainBinding
import com.example.budgetsphere.ui.AddExpenseFragment
import com.example.budgetsphere.ui.AnalyticsFragment
import com.example.budgetsphere.ui.BudgetGoalsFragment
import com.example.budgetsphere.ui.CategoryTotalsFragment
import com.example.budgetsphere.ui.ExpenseListFragment

// DashboardFragment is in root package com.example.budgetsphere (no import needed)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If no user is logged in, go to login screen
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.getString("username", null) == null) {
            Log.d(TAG, "No user — redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "MainActivity created")

        // Show dashboard on launch
        loadFragment(DashboardFragment())

        // bottomNavigationView matches ID in activity_main.xml
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> { loadFragment(DashboardFragment());      true }
                R.id.nav_add       -> { loadFragment(AddExpenseFragment());     true }
                R.id.nav_list      -> { loadFragment(ExpenseListFragment());    true }
                R.id.nav_totals    -> { loadFragment(CategoryTotalsFragment()); true }
                R.id.nav_goals     -> { loadFragment(BudgetGoalsFragment());    true }
                else               -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_analytics -> {
                loadFragment(AnalyticsFragment())
                true
            }
            R.id.action_logout -> {
                getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}