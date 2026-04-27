package com.codecash.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.ExpenseEntry
import com.codecash.app.data.entity.Category
import com.codecash.app.databinding.ActivityAddExpenseBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager

    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Long? = null
    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var currentPhotoPath: String? = null
    private var photoUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            binding.ivPhotoPreview.setImageURI(Uri.fromFile(File(currentPhotoPath!!)))
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.VISIBLE
            binding.btnAddPhoto.text = "Change Photo"
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentPhotoPath = copyUriToFile(it)
            binding.ivPhotoPreview.setImageURI(it)
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.VISIBLE
            binding.btnAddPhoto.text = "Change Photo"
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        setDefaultDate()
        setupDateTimePickers()
        loadCategories()
        setupPhotoButtons()
        binding.btnSave.setOnClickListener { validateAndSave() }
    }

    private fun setDefaultDate() {
        val cal = Calendar.getInstance()
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        binding.btnDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
    }

    private fun setupDateTimePickers() {
        binding.btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.btnDate.text = String.format("%02d %s %04d", d,
                    SimpleDateFormat("MMM", Locale.getDefault())
                        .format(Calendar.getInstance().apply { set(Calendar.MONTH, m) }.time), y)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnStartTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                selectedStartTime = String.format("%02d:%02d", h, min)
                binding.btnStartTime.text = selectedStartTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        binding.btnEndTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                selectedEndTime = String.format("%02d:%02d", h, min)
                binding.btnEndTime.text = selectedEndTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            categories = db.categoryDao().getCategoriesByUserOnce(session.getUserId())
            if (categories.isEmpty()) {
                binding.tvNoCategoriesWarning.visibility = View.VISIBLE
            } else {
                binding.tvNoCategoriesWarning.visibility = View.GONE
                val names = listOf("Select category") + categories.map { it.name }
                binding.spinnerCategory.adapter = ArrayAdapter(
                    this@AddExpenseActivity,
                    android.R.layout.simple_spinner_item, names
                ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
        }
    }

    private fun setupPhotoButtons() {
        binding.btnAddPhoto.setOnClickListener {
            android.app.AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Add Photo")
                .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) launchCamera()
                            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        1 -> pickImageLauncher.launch("image/*")
                    }
                }.show()
        }
        binding.btnRemovePhoto.setOnClickListener {
            currentPhotoPath = null
            binding.ivPhotoPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
            binding.btnAddPhoto.text = "Add Photo"
        }
    }

    private fun launchCamera() {
        val file = File.createTempFile(
            "CODECASH_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_",
            ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        ).also { currentPhotoPath = it.absolutePath }
        photoUri = FileProvider.getUriForFile(this, "com.codecash.app.provider", file)
        takePictureLauncher.launch(photoUri)
    }

    private fun copyUriToFile(uri: Uri): String? {
        return try {
            val dest = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "CODECASH_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg")
            contentResolver.openInputStream(uri)?.use { it.copyTo(dest.outputStream()) }
            dest.absolutePath
        } catch (e: Exception) { null }
    }

    private fun validateAndSave() {
        val description = binding.etDescription.text.toString().trim()
        val amountStr = binding.etAmount.text.toString().trim()
        var valid = true

        if (selectedDate.isEmpty()) { Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show(); valid = false }
        if (selectedStartTime.isEmpty()) { Toast.makeText(this, "Select start time", Toast.LENGTH_SHORT).show(); valid = false }
        if (selectedEndTime.isEmpty()) { Toast.makeText(this, "Select end time", Toast.LENGTH_SHORT).show(); valid = false }
        if (description.isEmpty()) { binding.tilDescription.error = "Description required"; valid = false }
        else binding.tilDescription.error = null
        if (amountStr.isEmpty()) { binding.tilAmount.error = "Amount required"; valid = false }
        else binding.tilAmount.error = null

        if (!valid) return

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { binding.tilAmount.error = "Enter a valid amount"; return }

        val catIndex = binding.spinnerCategory.selectedItemPosition
        selectedCategoryId = if (catIndex > 0 && categories.isNotEmpty()) categories[catIndex - 1].id else null

        binding.btnSave.isEnabled = false
        lifecycleScope.launch {
            try {
                db.expenseEntryDao().insertEntry(ExpenseEntry(
                    userId = session.getUserId(),
                    categoryId = selectedCategoryId,
                    description = description,
                    date = selectedDate,
                    startTime = selectedStartTime,
                    endTime = selectedEndTime,
                    amount = amount,
                    photoPath = currentPhotoPath
                ))
                Toast.makeText(this@AddExpenseActivity, "Expense saved", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                Toast.makeText(this@AddExpenseActivity, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
