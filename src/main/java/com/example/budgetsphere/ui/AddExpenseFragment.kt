package com.example.budgetsphere.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.example.budgetsphere.R
import com.example.budgetsphere.data.AppDatabase
import com.example.budgetsphere.data.Expense
import com.example.budgetsphere.databinding.FragmentAddExpenseBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddExpenseFragment"

    private var selectedImagePath: String? = null
    private var cameraImageFile:   File?   = null
    private var categoryIds:       List<Int> = emptyList()

    // ── Activity result launchers ─────────────────────────────

    // Camera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            cameraImageFile?.absolutePath?.let { path ->
                selectedImagePath = path
                showImagePreview(path)
            }
        }
    }

    // Gallery
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val path = copyUriToFile(uri)
                if (path != null) {
                    selectedImagePath = path
                    showImagePreview(path)
                }
            }
        }
    }

    // Camera permission
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Default date = today
        binding.etDate.setText(LocalDate.now().toString())
        binding.etDate.setOnClickListener { pickDate() }

        // Load categories into spinner
        loadCategories()

        // Image buttons
        binding.btnCamera.setOnClickListener { checkCameraPermissionAndLaunch() }
        binding.btnGallery.setOnClickListener { launchGallery() }

        // Remove image
        binding.btnRemoveImage.setOnClickListener {
            selectedImagePath = null
            binding.ivReceiptPreview.visibility  = View.GONE
            binding.btnRemoveImage.visibility    = View.GONE
        }

        // Save
        binding.btnSave.setOnClickListener { saveExpense() }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val db   = AppDatabase.getInstance(requireContext())
                val cats = db.categoryDao().getAllCategoriesOnce()
                categoryIds = cats.map { it.id }
                val names = cats.map { it.name }
                requireActivity().runOnUiThread {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        names
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCategory.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories: ${e.message}")
            }
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            cameraImageFile = File.createTempFile("RECEIPT_${timeStamp}_", ".jpg", storageDir)

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                cameraImageFile!!
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
            cameraLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Camera error: ${e.message}")
            Toast.makeText(requireContext(), "Could not open camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun copyUriToFile(uri: Uri): String? {
        return try {
            val timeStamp  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val destFile   = File(storageDir, "RECEIPT_${timeStamp}.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gallery copy error: ${e.message}")
            null
        }
    }

    private fun showImagePreview(path: String) {
        val bitmap = BitmapFactory.decodeFile(path)
        if (bitmap != null) {
            binding.ivReceiptPreview.setImageBitmap(bitmap)
            binding.ivReceiptPreview.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility   = View.VISIBLE
        }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> binding.etDate.setText("%04d-%02d-%02d".format(y, m + 1, d)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveExpense() {
        val title  = binding.etTitle.text.toString().trim()
        val amount = binding.etAmount.text.toString().toDoubleOrNull()
        val date   = binding.etDate.text.toString().trim()
        val note   = binding.etNote.text.toString().trim()
        val catIdx = binding.spinnerCategory.selectedItemPosition

        if (title.isEmpty()) {
            binding.etTitle.error = "Title required"
            return
        }
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Valid amount required"
            return
        }
        if (date.isEmpty()) {
            binding.etDate.error = "Date required"
            return
        }
        if (categoryIds.isEmpty()) {
            Toast.makeText(requireContext(), "No categories found — add one first", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            title      = title,
            amount     = amount,
            date       = date,
            categoryId = categoryIds[catIdx],
            note       = note.ifEmpty { null },
            imagePath  = selectedImagePath
        )

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                db.expenseDao().insert(expense)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
                    clearForm()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Save error: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error saving expense", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun clearForm() {
        binding.etTitle.setText("")
        binding.etAmount.setText("")
        binding.etDate.setText(LocalDate.now().toString())
        binding.etNote.setText("")
        binding.spinnerCategory.setSelection(0)
        selectedImagePath = null
        binding.ivReceiptPreview.visibility = View.GONE
        binding.btnRemoveImage.visibility   = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}