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

    // SeekBar max = 50000 (R 50,000)
    private val MAX_AMOUNT = 50000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set seekbar max
        binding.seekBarMin.max = MAX_AMOUNT
        binding.seekBarMax.max = MAX_AMOUNT

        // Back button
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Load existing goal from DB
        loadExistingGoal()

        // Min seekbar listener
        binding.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvMinGoal.text = "Min: R %,d".format(progress)
                Log.d(TAG, "Min goal changed: $progress")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Max seekbar listener
        binding.seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvMaxGoal.text = "Max: R %,d".format(progress)
                Log.d(TAG, "Max goal changed: $progress")
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save button
        binding.btnSaveGoals.setOnClickListener {
            saveGoals()
        }
    }

    private fun loadExistingGoal() {
        lifecycleScope.launch {
            val db   = AppDatabase.getInstance(requireContext())
            val goal = db.budgetGoalDao().getLatestGoal()

            if (goal != null) {
                Log.d(TAG, "Loaded existing goal: min=${goal.minGoal}, max=${goal.maxGoal}")
                requireActivity().runOnUiThread {
                    val minVal = goal.minGoal.toInt().coerceIn(0, MAX_AMOUNT)
                    val maxVal = goal.maxGoal.toInt().coerceIn(0, MAX_AMOUNT)

                    binding.seekBarMin.progress = minVal
                    binding.seekBarMax.progress = maxVal
                    binding.tvMinGoal.text = "Min: R %,d".format(minVal)
                    binding.tvMaxGoal.text = "Max: R %,d".format(maxVal)
                }
            }
        }
    }

    private fun saveGoals() {
        val minVal = binding.seekBarMin.progress.toDouble()
        val maxVal = binding.seekBarMax.progress.toDouble()

        // Validation
        if (minVal <= 0 && maxVal <= 0) {
            Toast.makeText(requireContext(), "Please set at least one goal", Toast.LENGTH_SHORT).show()
            return
        }
        if (maxVal < minVal) {
            Toast.makeText(requireContext(), "Maximum goal must be greater than minimum", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            db.budgetGoalDao().insertOrReplace(
                BudgetGoal(
                    minGoal = minVal,
                    maxGoal = maxVal,
                    id = TODO(),
                    createdAt = TODO()
                )
            )
            Log.d(TAG, "Goals saved: min=$minVal, max=$maxVal")

            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "Goals saved! Min: R ${"%.0f".format(minVal)}, Max: R ${"%.0f".format(maxVal)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}