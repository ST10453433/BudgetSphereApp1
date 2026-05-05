package com.example.budgetsphere.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCharts()
    }

    private fun loadCharts() {
        lifecycleScope.launch {
            val db     = AppDatabase.getInstance(requireContext())
            val today  = LocalDate.now()
            val start  = today.withDayOfMonth(1).toString()
            val end    = today.toString()
            val totals = db.expenseDao().getCategoryTotals(start, end)
            val cats   = db.categoryDao().getAllCategoriesOnce()
            val catMap = cats.associateBy { it.id }

            val pieEntries = totals.map { t ->
                PieEntry(t.total.toFloat(), catMap[t.categoryId]?.name ?: "Other")
            }
            val barEntries = totals.mapIndexed { i, t ->
                BarEntry(i.toFloat(), t.total.toFloat())
            }
            val barLabels = totals.map { catMap[it.categoryId]?.name ?: "Other" }

            requireActivity().runOnUiThread {
                setupPieChart(pieEntries)
                setupBarChart(barEntries, barLabels)
            }
        }
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        if (entries.isEmpty()) return
        val colours = listOf(
            Color.parseColor("#1AAFAA"), Color.parseColor("#1565C0"),
            Color.parseColor("#6A1B9A"), Color.parseColor("#F57C00"),
            Color.parseColor("#E53935")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors         = colours
            valueTextSize  = 12f
            valueTextColor = Color.WHITE
            sliceSpace     = 3f
        }
        binding.pieChartCategories.apply {
            data                  = PieData(dataSet)
            isDrawHoleEnabled     = true
            holeRadius            = 40f
            setHoleColor(Color.WHITE)
            description.isEnabled = false
            legend.isEnabled      = true
            setEntryLabelColor(Color.DKGRAY)
            setEntryLabelTextSize(11f)
            animateY(800)
            invalidate()
        }
    }

    private fun setupBarChart(entries: List<BarEntry>, labels: List<String>) {
        if (entries.isEmpty()) return
        val colours = listOf(
            Color.parseColor("#1AAFAA"), Color.parseColor("#1565C0"),
            Color.parseColor("#6A1B9A"), Color.parseColor("#F57C00")
        )
        val dataSet = BarDataSet(entries, "Spending by Category").apply {
            colors        = colours
            valueTextSize = 11f
        }
        binding.barChartDailySpending.apply {
            data = BarData(dataSet).apply { barWidth = 0.5f }
            xAxis.apply {
                valueFormatter       = IndexAxisValueFormatter(labels)
                granularity          = 1f
                isGranularityEnabled = true
                labelRotationAngle   = -20f
            }
            axisRight.isEnabled   = false
            description.isEnabled = false
            legend.isEnabled      = false
            setFitBars(true)
            animateY(800)
            invalidate()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}