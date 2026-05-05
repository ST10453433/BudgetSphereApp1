package com.example.budgetsphere


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetsphere.databinding.ActivityBudgetGoalsBinding
import com.example.budgetsphere.ui.BudgetGoalsFragment

class BudgetGoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetGoalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.budgetGoalsContainer, BudgetGoalsFragment())
                .commit()
        }
    }
}