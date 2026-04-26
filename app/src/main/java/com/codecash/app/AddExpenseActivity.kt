package com.codecash.app

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.CategoryEntity
import com.codecash.app.data.entity.ExpenseEntity
import com.codecash.app.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private val TAG = "AddExpenseActivity"
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private var categories = listOf<CategoryEntity>()
    private var selectedCategoryId: Int = -1

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        setupCategorySpinner()
        setupPhotoButton()
        setupSaveButton()
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupDatePicker() {
        updateDateDisplay()
        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                        selectedDate = timeInMillis
                        updateDateDisplay()
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        binding.etDate.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate)))
    }

    private fun setupCategorySpinner() {
        lifecycleScope.launch {
            categories = AppDatabase.getDatabase(this@AddExpenseActivity).categoryDao().getAllCategoriesList()
            if (categories.isEmpty()) {
                Toast.makeText(this@AddExpenseActivity, "Please add categories first", Toast.LENGTH_LONG).show()
            }
            val adapter = ArrayAdapter(
                this@AddExpenseActivity,
                android.R.layout.simple_spinner_item,
                categories.map { it.name }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedCategoryId = categories[position].id
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupPhotoButton() {
        binding.btnTakePhoto.setOnClickListener { dispatchTakePictureIntent() }
        binding.btnRemovePhoto.setOnClickListener {
            photoUri = null
            currentPhotoPath = null
            binding.ivPhotoPreview.setImageDrawable(null)
            binding.ivPhotoPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e(TAG, "Error creating file", ex)
                    null
                }
                photoFile?.also {
                    val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                    photoUri = uri
                    currentPhotoPath = it.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            currentPhotoPath?.let { path ->
                binding.ivPhotoPreview.setImageURI(Uri.fromFile(File(path)))
                binding.ivPhotoPreview.visibility = View.VISIBLE
                binding.btnRemovePhoto.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveExpense.setOnClickListener {
            val amountStr = binding.etAmount.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            when {
                amountStr.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.enter_amount), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                description.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.enter_description), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                selectedCategoryId == -1 -> {
                    Toast.makeText(this, getString(R.string.select_category_first), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val expense = ExpenseEntity(
                        amount = amount,
                        date = selectedDate,
                        description = description,
                        categoryId = selectedCategoryId,
                        photoPath = currentPhotoPath
                    )
                    AppDatabase.getDatabase(this@AddExpenseActivity).expenseDao().insert(expense)
                    Toast.makeText(this@AddExpenseActivity, getString(R.string.expense_added), Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving expense", e)
                    Toast.makeText(this@AddExpenseActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
