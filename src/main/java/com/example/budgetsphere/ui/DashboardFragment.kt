package com.example.budgetsphere

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.budgetsphere.ui.AddExpenseFragment
import com.example.budgetsphere.ui.CategoryTotalsFragment
import com.example.budgetsphere.ui.ExpenseListFragment

class DashboardFragment : Fragment() {

    private lateinit var tvAvatar: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var btnQuickAdd: LinearLayout
    private lateinit var btnQuickHistory: LinearLayout
    private lateinit var btnQuickGame: LinearLayout
    private lateinit var btnQuickSimulator: LinearLayout
    private lateinit var tvBudgetDetails: TextView
    private lateinit var tvBudgetSpent: TextView
    private lateinit var tvBudgetTotal: TextView
    private lateinit var progressBudget: ProgressBar
    private lateinit var tvBudgetPercent: TextView
    private lateinit var tvBudgetRemaining: TextView
    private lateinit var tvSeeAll: TextView
    private lateinit var tvGroceriesAmount: TextView
    private lateinit var tvTransportAmount: TextView
    private lateinit var tvEntertainmentAmount: TextView
    private lateinit var tvUtilitiesAmount: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        populateDashboard()
        setClickListeners()
    }

    private fun initViews(view: View) {
        tvAvatar              = view.findViewById(R.id.tvAvatar)
        tvGreeting            = view.findViewById(R.id.tvGreeting)
        tvUserName            = view.findViewById(R.id.tvUserName)
        tvTotalBalance        = view.findViewById(R.id.tvTotalBalance)
        tvIncome              = view.findViewById(R.id.tvIncome)
        tvExpenses            = view.findViewById(R.id.tvExpenses)
        btnQuickAdd           = view.findViewById(R.id.btnQuickAdd)
        btnQuickHistory       = view.findViewById(R.id.btnQuickHistory)
        btnQuickGame          = view.findViewById(R.id.btnQuickGame)
        btnQuickSimulator     = view.findViewById(R.id.btnQuickSimulator)
        tvBudgetDetails       = view.findViewById(R.id.tvBudgetDetails)
        tvBudgetSpent         = view.findViewById(R.id.tvBudgetSpent)
        tvBudgetTotal         = view.findViewById(R.id.tvBudgetTotal)
        progressBudget        = view.findViewById(R.id.progressBudget)
        tvBudgetPercent       = view.findViewById(R.id.tvBudgetPercent)
        tvBudgetRemaining     = view.findViewById(R.id.tvBudgetRemaining)
        tvSeeAll              = view.findViewById(R.id.tvSeeAll)
        tvGroceriesAmount     = view.findViewById(R.id.tvGroceriesAmount)
        tvTransportAmount     = view.findViewById(R.id.tvTransportAmount)
        tvEntertainmentAmount = view.findViewById(R.id.tvEntertainmentAmount)
        tvUtilitiesAmount     = view.findViewById(R.id.tvUtilitiesAmount)
    }

    private fun populateDashboard() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning 🌤"
            hour < 17 -> "Good afternoon ☀️"
            else      -> "Good evening 🌙"
        }
        tvGreeting.text     = greeting
        tvUserName.text     = requireActivity()
            .getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
            .getString("username", "User") ?: "User"
        tvAvatar.text       = tvUserName.text.toString().take(2).uppercase()
        tvTotalBalance.text = "R 24,850.00"
        tvIncome.text       = "R 32,000"
        tvExpenses.text     = "R 7,150"

        val spent     = 7150
        val budget    = 12000
        val percent   = ((spent.toFloat() / budget.toFloat()) * 100).toInt()
        val remaining = budget - spent

        tvBudgetSpent.text     = "R ${String.format("%,d", spent)}"
        tvBudgetTotal.text     = "R ${String.format("%,d", budget)}"
        progressBudget.progress = percent
        tvBudgetPercent.text   = "$percent% used"
        tvBudgetRemaining.text = "R ${String.format("%,d", remaining)} remaining"

        tvGroceriesAmount.text     = "R 1,200"
        tvTransportAmount.text     = "R 850"
        tvEntertainmentAmount.text = "R 900"
        tvUtilitiesAmount.text     = "R 600"
    }

    private fun setClickListeners() {
        btnQuickAdd.setOnClickListener {
            navigateToFragment(AddExpenseFragment())
        }
        btnQuickHistory.setOnClickListener {
            navigateToFragment(ExpenseListFragment())
        }
        btnQuickGame.setOnClickListener {
            Toast.makeText(requireContext(), "Challenge Mode coming soon!", Toast.LENGTH_SHORT).show()
        }
        btnQuickSimulator.setOnClickListener {
            Toast.makeText(requireContext(), "Future Impact Simulator coming soon!", Toast.LENGTH_SHORT).show()
        }
        tvBudgetDetails.setOnClickListener {
            startActivity(Intent(requireContext(), BudgetGoalsActivity::class.java))
        }
        tvSeeAll.setOnClickListener {
            navigateToFragment(CategoryTotalsFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}