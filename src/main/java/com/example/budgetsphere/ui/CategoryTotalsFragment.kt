
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
import com.example.budgetsphere.adapters.CategoryTotalsAdapter
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.databinding.FragmentCategoryTotalsBinding
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class CategoryTotalsFragment : Fragment() {

    private var _binding: FragmentCategoryTotalsBinding? = null
    private val binding get() = _binding!!
    private val TAG = "CategoryTotalsFragment"
    private lateinit var adapter: CategoryTotalsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryTotalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryTotalsAdapter()
        binding.recyclerTotals.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTotals.adapter = adapter

        val today = LocalDate.now()
        val start = today.withDayOfMonth(1).toString()
        val end   = today.toString()
        binding.etStartDate.setText(start)
        binding.etEndDate.setText(end)
        loadTotals(start, end)

        binding.etStartDate.setOnClickListener {
            pickDate { binding.etStartDate.setText(it); reloadTotals() }
        }
        binding.etEndDate.setOnClickListener {
            pickDate { binding.etEndDate.setText(it); reloadTotals() }
        }
        binding.btnFilter.setOnClickListener { reloadTotals() }
    }

    private fun reloadTotals() {
        val start = binding.etStartDate.text.toString()
        val end   = binding.etEndDate.text.toString()
        if (start.isNotEmpty() && end.isNotEmpty()) loadTotals(start, end)
    }

    private fun loadTotals(start: String, end: String) {
        Log.d(TAG, "Loading category totals $start → $end")
        lifecycleScope.launch {
            try {
                val db     = AppDatabase.getInstance(requireContext())
                val totals = db.expenseDao().getCategoryTotals(start, end)
                val cats   = db.categoryDao().getAllCategoriesOnce()
                val catMap = cats.associateBy { it.id }
                val display = totals
                    .map { t -> Pair(catMap[t.categoryId]?.name ?: "Unknown", t.total) }
                    .sortedByDescending { it.second }

                requireActivity().runOnUiThread {
                    adapter.submitData(display)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
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