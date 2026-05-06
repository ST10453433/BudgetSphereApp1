
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

        // Set up RecyclerView
        adapter = ExpenseAdapter(requireContext())
        binding.recyclerExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExpenses.adapter = adapter

        // Default: show current month
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1).toString()
        val end   = today.toString()
        binding.etStartDate.setText(start)
        binding.etEndDate.setText(end)
        loadExpenses(start, end)

        // Date pickers
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

        binding.btnFilter.setOnClickListener { reloadList() }
    }

    private fun reloadList() {
        val start = binding.etStartDate.text.toString()
        val end   = binding.etEndDate.text.toString()
        if (start.isNotEmpty() && end.isNotEmpty()) {
            loadExpenses(start, end)
        }
    }

    // KEY FIX: uses suspend getExpensesBetweenOnce() NOT LiveData
    // This is why the app was crashing before
    private fun loadExpenses(start: String, end: String) {
        Log.d(TAG, "Loading expenses: $start to $end")
        lifecycleScope.launch {
            try {
                val db       = AppDatabase.getInstance(requireContext())
                // This returns List<Expense> directly — safe in coroutine
                val expenses = db.expenseDao().getExpensesBetweenOnce(start, end)
                val cats     = db.categoryDao().getAllCategoriesOnce()
                val catMap   = cats.associateBy { it.id }

                requireActivity().runOnUiThread {
                    adapter.submitData(expenses, catMap)
                    if (expenses.isEmpty()) {
                        binding.tvExpenseCount.text = "No expenses found for this period"
                    } else {
                        val total = expenses.sumOf { it.amount }
                        binding.tvExpenseCount.text =
                            "${expenses.size} expense(s)  |  Total: R %.2f".format(total)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading expenses: ${e.message}")
                requireActivity().runOnUiThread {
                    binding.tvExpenseCount.text = "Error loading expenses. Please try again."
                }
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