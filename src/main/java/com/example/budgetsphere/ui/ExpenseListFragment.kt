
package com.example.budgetsphere.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetsphere.MainActivity
import com.example.budgetsphere.R
import com.example.budgetsphere.adapters.ExpenseAdapter
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.databinding.FragmentExpenseListBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class ExpenseListFragment : Fragment() {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ExpenseListFragment"
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Setup RecyclerView ────────────────────────────────
        adapter = ExpenseAdapter(requireContext()) { expense ->
            // Delete on long-press — refreshes list after
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    db.expenseDao().delete(expense)
                    reloadList()
                } catch (e: Exception) {
                    Log.e(TAG, "Delete error: ${e.message}")
                }
            }
        }
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = adapter

        // ── Default date range: current month ─────────────────
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1).toString()
        val end   = today.toString()
        binding.etStartDate.setText(start)
        binding.etEndDate.setText(end)
        loadExpenses(start, end)

        // ── Date pickers ──────────────────────────────────────
        binding.etStartDate.setOnClickListener {
            pickDate { date ->
                binding.etStartDate.setText(date)
                reloadList()
            }
        }
        binding.etEndDate.setOnClickListener {
            pickDate { date ->
                binding.etEndDate.setText(date)
                reloadList()
            }
        }

        // ── Filter button ─────────────────────────────────────
        binding.btnFilter.setOnClickListener { reloadList() }

        // ── "All Time" shortcut button ────────────────────────
        binding.btnAllTime.setOnClickListener {
            binding.etStartDate.setText("2000-01-01")
            binding.etEndDate.setText(LocalDate.now().toString())
            reloadList()
        }

        // ── "This Month" shortcut button ──────────────────────
        binding.btnThisMonth.setOnClickListener {
            val now = LocalDate.now()
            binding.etStartDate.setText(now.withDayOfMonth(1).toString())
            binding.etEndDate.setText(now.toString())
            reloadList()
        }

        // ── Back to Dashboard ─────────────────────────────────
        binding.btnBackDashboard.setOnClickListener {
            (activity as? MainActivity)?.navigateTo(R.id.nav_dashboard)
        }
    }

    private fun reloadList() {
        val start = binding.etStartDate.text.toString()
        val end   = binding.etEndDate.text.toString()
        if (start.isNotEmpty() && end.isNotEmpty()) loadExpenses(start, end)
    }

    private fun loadExpenses(start: String, end: String) {
        Log.d(TAG, "Loading expenses from $start to $end")
        lifecycleScope.launch {
            try {
                val db       = AppDatabase.getInstance(requireContext())
                val expenses = db.expenseDao().getExpensesBetweenOnce(start, end)
                val cats     = db.categoryDao().getAllCategoriesOnce()
                val catMap   = cats.associateBy { it.id }
                val total    = expenses.sumOf { it.amount }

                val b = _binding ?: return@launch
                requireActivity().runOnUiThread {
                    adapter.submitData(expenses, catMap)

                    // Summary bar
                    b.tvExpenseCount.text =
                        "${expenses.size} expense(s)  •  Total: R %.2f".format(total)

                    // Empty state
                    b.tvEmpty.visibility =
                        if (expenses.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading expenses: ${e.message}")
            }
        }
    }

    private fun pickDate(onPicked: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> onPicked("%04d-%02d-%02d".format(y, m + 1, d)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}