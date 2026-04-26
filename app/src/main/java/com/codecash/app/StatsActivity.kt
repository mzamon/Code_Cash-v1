package com.codecash.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.CategoryStatAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityStatsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatsBinding
    private lateinit var categoryStatAdapter: CategoryStatAdapter
    private val TAG = "StatsActivity"
    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupDatePickers()
        binding.btnBack.setOnClickListener { finish() }

        // Set default dates to current month
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
        loadStats()
    }

    private fun setupRecyclerView() {
        categoryStatAdapter = CategoryStatAdapter(emptyList())
        binding.rvStats.layoutManager = LinearLayoutManager(this)
        binding.rvStats.adapter = categoryStatAdapter
    }

    private fun setupDatePickers() {
        binding.btnStartDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = startDate }
            DatePickerDialog(this, { _, year, month, day ->
                Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                    startDate = timeInMillis
                    updateDateDisplays()
                    loadStats()
                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnEndDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = endDate }
            DatePickerDialog(this, { _, year, month, day ->
                Calendar.getInstance().apply {
                    set(year, month, day, 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                    endDate = timeInMillis
                    updateDateDisplays()
                    loadStats()
                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateDisplays() {
        binding.tvStartDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(startDate))
        binding.tvEndDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(endDate))
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@StatsActivity)
                val total = db.expenseDao().getTotalExpensesInPeriod(startDate, endDate) ?: 0.0
                binding.tvGrandTotal.text = "Grand Total: R${String.format("%,.2f", total)}"

                val categories = db.categoryDao().getAllCategoriesList()
                val stats = mutableListOf<CategoryStat>()

                if (total > 0) {
                    for (category in categories) {
                        val catTotal = db.expenseDao().getTotalByCategoryInPeriod(category.id, startDate, endDate) ?: 0.0
                        if (catTotal > 0) {
                            val percentage = ((catTotal / total) * 100).toInt()
                            stats.add(CategoryStat(category.name, catTotal, percentage))
                        }
                    }
                }

                // Sort by percentage descending
                stats.sortByDescending { it.percentage }

                categoryStatAdapter.updateData(stats)

                if (stats.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvStats.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvStats.visibility = View.VISIBLE
                }

                Log.d(TAG, "Loaded ${stats.size} category stats")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stats", e)
            }
        }
    }
}
