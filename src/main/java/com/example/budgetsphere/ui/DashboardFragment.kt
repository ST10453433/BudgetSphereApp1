// ============================================================
// FILE: ui/DashboardFragment.kt
// ============================================================
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

        val prefs    = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "User") ?: "User"

        loadUserFullName(username)
        loadMonthSummary()

        // ── Quick-action buttons → navigate to correct tab ────
        binding.btnQuickAdd.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_add)
        }
        binding.btnQuickHistory.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_list)
        }
        binding.btnQuickTotals.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_totals)
        }
        binding.btnQuickGoals.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_goals)
        }

        // ── Category summary row clicks → go to totals tab ───
        binding.tvCategorySummary.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_totals)
        }

        // ── Expense count row click → go to history tab ───────
        binding.tvExpenseCount.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_list)
        }

        // ── Goal info / progress bar click → go to goals tab ──
        binding.tvGoalInfo.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_goals)
        }
        binding.progressGoal.setOnClickListener {
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

    private fun loadUserFullName(username: String) {
        lifecycleScope.launch {
            try {
                val db   = AppDatabase.getInstance(requireContext())
                val user = db.userDao().findByUsername(username)
                val displayName = user?.fullName ?: username
                _binding?.tvWelcome?.text = "Welcome, $displayName 👋"
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user: ${e.message}")
                _binding?.tvWelcome?.text = "Welcome, $username 👋"
            }
        }
    }

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
                val cats     = db.categoryDao().getAllCategoriesOnce()
                val catMap   = cats.associateBy { it.id }
                val totals   = db.expenseDao().getCategoryTotals(startOfMonth, endOfMonth)

                // Switch to main thread to update UI
                val b = _binding ?: return@launch   // fragment may have been destroyed
                requireActivity().runOnUiThread {
                    b.tvTotalSpent.text    = "R %.2f".format(total)
                    b.tvExpenseCount.text  = "${expenses.size} expense(s) this month — tap to view"

                    if (goal != null) {
                        b.tvGoalInfo.text          = "Budget: R %.0f – R %.0f  (tap for details)".format(goal.minGoal, goal.maxGoal)
                        b.progressGoal.max         = goal.maxGoal.toInt()
                        b.progressGoal.progress    = total.toInt().coerceAtMost(goal.maxGoal.toInt())
                        if (total > goal.maxGoal) {
                            b.tvGoalInfo.setTextColor(
                                resources.getColor(android.R.color.holo_red_light, null)
                            )
                        }
                    } else {
                        b.tvGoalInfo.text = "No budget goal set — tap to set one"
                    }

                    if (totals.isNotEmpty()) {
                        val sb = StringBuilder("Top categories (tap to see all):\n")
                        totals.sortedByDescending { it.total }.take(3).forEach { t ->
                            val catName = catMap[t.categoryId]?.name ?: "Other"
                            sb.append("$catName: R %.2f\n".format(t.total))
                        }
                        b.tvCategorySummary.text = sb.toString().trim()
                    } else {
                        b.tvCategorySummary.text = "No expenses yet this month — tap to add"
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