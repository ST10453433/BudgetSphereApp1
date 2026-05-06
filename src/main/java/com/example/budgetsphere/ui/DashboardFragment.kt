package com.example.budgetsphere.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetsphere.LoginActivity
import com.example.budgetsphere.MainActivity
import com.example.budgetsphere.R
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val TAG = "DashboardFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "User") ?: "User"

        // Load full name from DB to show on dashboard
        loadUserFullName(username)

        // Load this month's spending summary
        loadMonthSummary()

        // ── Quick action buttons ──────────────────────────────
        // Each button switches to the relevant tab in MainActivity

        // Add Expense
        binding.btnQuickAdd.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_add)
        }

        // History
        binding.btnQuickHistory.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_list)
        }

        // Category Totals
        binding.btnQuickTotals.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_totals)
        }

        // Budget Goals
        binding.btnQuickGoals.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_goals)
        }

        // ── Logout ────────────────────────────────────────────
        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "User logging out: $username")
            prefs.edit().remove("username").apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    // Load real full name from database
    private fun loadUserFullName(username: String) {
        lifecycleScope.launch {
            try {
                val db   = AppDatabase.getInstance(requireContext())
                val user = db.userDao().findByUsername(username)
                requireActivity().runOnUiThread {
                    val displayName = user?.fullName ?: username
                    binding.tvWelcome.text = "Welcome, $displayName 👋"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user: ${e.message}")
                requireActivity().runOnUiThread {
                    binding.tvWelcome.text = "Welcome, $username 👋"
                }
            }
        }
    }

    // Load this month's spending vs goal
    private fun loadMonthSummary() {
        lifecycleScope.launch {
            try {
                val db           = AppDatabase.getInstance(requireContext())
                val today        = LocalDate.now()
                val startOfMonth = today.withDayOfMonth(1).toString()
                val endOfMonth   = today.toString()

                val expenses = db.expenseDao().getExpensesBetweenOnce(startOfMonth, endOfMonth)
                val total    = expenses.sumOf { it.amount }
                val goal     = db.budgetGoalDao().getLatestGoal()

                // Category breakdown for dashboard
                val cats     = db.categoryDao().getAllCategoriesOnce()
                val catMap   = cats.associateBy { it.id }
                val totals   = db.expenseDao().getCategoryTotals(startOfMonth, endOfMonth)

                requireActivity().runOnUiThread {
                    // Total spent
                    binding.tvTotalSpent.text = "R %.2f".format(total)
                    binding.tvExpenseCount.text = "${expenses.size} expense(s) this month"

                    // Goal progress
                    if (goal != null) {
                        binding.tvGoalInfo.text =
                            "Budget: R %.0f – R %.0f".format(goal.minGoal, goal.maxGoal)
                        binding.progressGoal.max      = goal.maxGoal.toInt()
                        binding.progressGoal.progress = total.toInt().coerceAtMost(goal.maxGoal.toInt())

                        // Turn progress bar red if over max
                        if (total > goal.maxGoal) {
                            binding.tvGoalInfo.setTextColor(
                                resources.getColor(android.R.color.holo_red_light, null)
                            )
                        }
                    } else {
                        binding.tvGoalInfo.text = "No budget goal set — tap Goals to set one"
                    }

                    // Category summary text
                    if (totals.isNotEmpty()) {
                        val sb = StringBuilder()
                        totals.sortedByDescending { it.total }.take(3).forEach { t ->
                            val catName = catMap[t.categoryId]?.name ?: "Other"
                            sb.append("$catName: R %.2f\n".format(t.total))
                        }
                        binding.tvCategorySummary.text = sb.toString().trim()
                    } else {
                        binding.tvCategorySummary.text = "No expenses yet this month"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading summary: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}