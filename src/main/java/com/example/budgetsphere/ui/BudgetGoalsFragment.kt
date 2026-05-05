package com.example.budgetsphere.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.data.BudgetGoal
import com.example.budgetsphere.databinding.FragmentBudgetGoalsBinding
import kotlinx.coroutines.launch

class BudgetGoalsFragment : Fragment() {

    private var _binding: FragmentBudgetGoalsBinding? = null
    private val binding get() = _binding!!
    private val TAG = "BudgetGoalsFragment"

    private val MAX_GOAL = 50000
    private val STEP     = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seekBarMin.max = MAX_GOAL / STEP
        binding.seekBarMax.max = MAX_GOAL / STEP

        // Load saved goal from DB
        lifecycleScope.launch {
            val goal = AppDatabase.getInstance(requireContext()).budgetGoalDao().getLatestGoal()
            requireActivity().runOnUiThread {
                if (goal != null) {
                    binding.seekBarMin.progress = (goal.minGoal / STEP).toInt()
                    binding.seekBarMax.progress = (goal.maxGoal / STEP).toInt()
                    updateLabels(goal.minGoal, goal.maxGoal)
                } else {
                    updateLabels(0.0, 0.0)
                }
            }
        }

        binding.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                updateLabels(progress * STEP.toDouble(), binding.seekBarMax.progress * STEP.toDouble())
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        binding.seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                updateLabels(binding.seekBarMin.progress * STEP.toDouble(), progress * STEP.toDouble())
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        binding.btnSaveGoals.setOnClickListener {
            val minGoal = binding.seekBarMin.progress * STEP.toDouble()
            val maxGoal = binding.seekBarMax.progress * STEP.toDouble()

            if (minGoal >= maxGoal) {
                Toast.makeText(requireContext(), "Minimum must be less than maximum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                AppDatabase.getInstance(requireContext())
                    .budgetGoalDao().insertOrReplace(BudgetGoal(minGoal = minGoal, maxGoal = maxGoal))
                Log.d(TAG, "Goals saved: min=R$minGoal max=R$maxGoal")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Budget goals saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateLabels(min: Double, max: Double) {
        binding.tvMinGoal.text = "Min: R %.0f".format(min)
        binding.tvMaxGoal.text = "Max: R %.0f".format(max)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}