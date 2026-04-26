package com.codecash.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityExpenseDetailBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseDetailBinding
    private val TAG = "ExpenseDetailActivity"
    private var expenseId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseId = intent.getIntExtra("expense_id", -1)
        
        if (expenseId == -1) {
            Toast.makeText(this, "Invalid expense", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupDeleteButton()
        binding.btnBack.setOnClickListener { finish() }
        loadExpenseDetails()
    }

    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_confirm))
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    deleteExpense()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun deleteExpense() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@ExpenseDetailActivity)
                val expense = db.expenseDao().getExpenseById(expenseId)
                
                if (expense != null) {
                    // Delete photo file if exists
                    expense.photoPath?.let { path ->
                        try {
                            File(path).delete()
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to delete photo file", e)
                        }
                    }
                    
                    // Delete expense from database
                    db.expenseDao().delete(expense)
                    Toast.makeText(this@ExpenseDetailActivity, getString(R.string.expense_deleted), Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting expense", e)
                Toast.makeText(this@ExpenseDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadExpenseDetails() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@ExpenseDetailActivity)
                val expense = db.expenseDao().getExpenseById(expenseId)
                val category = expense?.categoryId?.let { db.categoryDao().getCategoryById(it) }
                
                if (expense != null) {
                    binding.tvDetailDescription.text = expense.description
                    binding.tvDetailCategory.text = category?.name ?: "Unknown"
                    binding.tvDetailDate.text = SimpleDateFormat("dd MMM yyyy 'at' HH:mm", Locale.getDefault()).format(Date(expense.date))
                    binding.tvDetailAmount.text = "R${String.format("%,.2f", expense.amount)}"
                    binding.tvDetailAmount.setTextColor(getColor(R.color.expense_red))
                    binding.tvDetailReference.text = "ID: #${expense.id.toString().padStart(6, '0')}"
                    
                    // Load photo if exists
                    expense.photoPath?.let { path ->
                        val photoFile = File(path)
                        if (photoFile.exists()) {
                            binding.ivDetailPhoto.setImageURI(android.net.Uri.fromFile(photoFile))
                            binding.ivDetailPhoto.visibility = android.view.View.VISIBLE
                        }
                    } ?: run {
                        binding.ivDetailPhoto.visibility = android.view.View.GONE
                    }
                } else {
                    Toast.makeText(this@ExpenseDetailActivity, "Expense not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading expense details", e)
                Toast.makeText(this@ExpenseDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
