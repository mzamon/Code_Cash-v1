package com.codecash.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.ExpenseAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityExpenseListBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseListBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private val TAG = "ExpenseListActivity"
    private var startDate: Long = 0
    private var endDate: Long = 0
    private var categories = emptyMap<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupDatePickers()
        setupFilterButton()
        binding.btnBack.setOnClickListener { finish() }
        
        // Set default dates to current month
        setDefaultDates()
    }

    private fun setDefaultDates() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        startDate = cal.timeInMillis
        
        cal.set(year, month, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        endDate = cal.timeInMillis
        
        updateDateDisplays()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(emptyList(), emptyMap()) { expense ->
            val intent = Intent(this, ExpenseDetailActivity::class.java)
            intent.putExtra("expense_id", expense.id)
            startActivity(intent)
        }
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = expenseAdapter
    }

    private fun setupDatePickers() {
        binding.btnStartDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = startDate }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                        startDate = timeInMillis
                        updateDateDisplays()
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnEndDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = endDate }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    Calendar.getInstance().apply {
                        set(year, month, day, 23, 59, 59)
                        set(Calendar.MILLISECOND, 999)
                        endDate = timeInMillis
                        updateDateDisplays()
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplays() {
        binding.tvStartDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(startDate))
        binding.tvEndDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(endDate))
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            loadExpenses()
        }
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@ExpenseListActivity)
                val expenses = db.expenseDao().getExpensesInPeriod(startDate, endDate)
                val categoryList = db.categoryDao().getAllCategoriesList()
                categories = categoryList.associateBy({ it.id }, { it.name })
                
                expenseAdapter.updateData(expenses, categories)
                
                if (expenses.isEmpty()) {
                    binding.tvEmpty.visibility = android.view.View.VISIBLE
                    binding.rvExpenses.visibility = android.view.View.GONE
                } else {
                    binding.tvEmpty.visibility = android.view.View.GONE
                    binding.rvExpenses.visibility = android.view.View.VISIBLE
                }
                
                Log.d(TAG, "Loaded ${expenses.size} expenses for period")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading expenses", e)
                Toast.makeText(this@ExpenseListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }
}
