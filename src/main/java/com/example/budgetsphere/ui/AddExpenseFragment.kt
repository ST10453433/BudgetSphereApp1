package com.example.budgetsphere.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.data.Category
import com.example.budgetsphere.data.Expense
import com.example.budgetsphere.databinding.FragmentAddExpenseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddExpenseFragment"

    private var photoUri: Uri? = null
    private var photoPath: String? = null
    private var categoryIds = listOf<Long>()

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            Glide.with(this).load(photoUri).into(binding.ivPhoto)
            binding.ivPhoto.visibility = View.VISIBLE
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            photoPath = it.toString()
            Glide.with(this).load(it).into(binding.ivPhoto)
            binding.ivPhoto.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadCategories()

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                binding.etDate.setText("%04d-%02d-%02d".format(y, m + 1, d))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etStartTime.setOnClickListener {
            showTimePicker { time -> binding.etStartTime.setText(time) }
        }

        binding.etEndTime.setOnClickListener {
            showTimePicker { time -> binding.etEndTime.setText(time) }
        }

        binding.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnUploadReceipt.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveExpense.setOnClickListener { saveExpense() }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, min ->
            onTimeSelected("%02d:%02d".format(h, min))
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun loadCategories() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(requireContext())
            var cats = db.categoryDao().getAllCategoriesOnce()

            if (cats.isEmpty()) {
                db.categoryDao().insert(Category(name = "Groceries", colorHex = "#1AAFAA"))
                db.categoryDao().insert(Category(name = "Transport", colorHex = "#1565C0"))
                db.categoryDao().insert(Category(name = "Entertainment", colorHex = "#6A1B9A"))
                db.categoryDao().insert(Category(name = "Utilities", colorHex = "#F57C00"))
                cats = db.categoryDao().getAllCategoriesOnce()
            }

            categoryIds = cats.map { it.id }
            val names = cats.map { it.name }

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter
            }
        }
    }

    private fun launchCamera() {
        try {
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("IMG_${stamp}_", ".jpg", storageDir)

            photoPath = photoFile.absolutePath
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoUri!!)
        } catch (e: Exception) {
            Log.e(TAG, "Camera error: ${e.message}")
        }
    }

    private fun saveExpense() {
        val date = binding.etDate.text.toString().trim()
        val start = binding.etStartTime.text.toString().trim()
        val end = binding.etEndTime.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()
        val amtStr = binding.etAmount.text.toString().trim()

        val amount = amtStr.toDoubleOrNull() ?: run {
            Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val idx = binding.spinnerCategory.selectedItemPosition
        if (idx == -1) return

        lifecycleScope.launch(Dispatchers.IO) {
            val expense = Expense(
                date = date,
                startTime = start,
                endTime = end,
                description = desc,
                amount = amount,
                categoryId = categoryIds[idx],
                photoPath = photoPath
            )
            AppDatabase.getInstance(requireContext()).expenseDao().insert(expense)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}