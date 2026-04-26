package com.codecash.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.BudgetGoalEntity
import com.codecash.app.databinding.ActivityBudgetBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetBinding
    private var selectedMonthYear: String = ""
    private val TAG = "BudgetActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cal = Calendar.getInstance()
        selectedMonthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(cal.time)
        updateMonthDisplay()

        binding.btnSelectMonth.setOnClickListener {
            DatePickerDialog(this, { _, year, month, _ ->
                val c = Calendar.getInstance().apply { set(year, month, 1) }
                selectedMonthYear = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(c.time)
                updateMonthDisplay()
                loadBudgetData()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1).show()
        }

        binding.btnSaveGoal.setOnClickListener {
            val minStr = binding.etMinGoal.text.toString().trim()
            val maxStr = binding.etMaxGoal.text.toString().trim()

            if (minStr.isEmpty() || maxStr.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_goals), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val min = minStr.toDoubleOrNull()
            val max = maxStr.toDoubleOrNull()

            if (min == null || max == null || min < 0 || max < 0) {
                Toast.makeText(this, "Enter valid amounts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (min > max) {
                Toast.makeText(this, getString(R.string.invalid_goals), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val goal = BudgetGoalEntity(
                        monthYear = selectedMonthYear,
                        minGoal = min,
                        maxGoal = max
                    )
                    AppDatabase.getDatabase(this@BudgetActivity).budgetGoalDao().insert(goal)
                    Toast.makeText(this@BudgetActivity, getString(R.string.goal_saved), Toast.LENGTH_SHORT).show()
                    loadBudgetData()
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving goal", e)
                    Toast.makeText(this@BudgetActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnBack.setOnClickListener { finish() }
        loadBudgetData()
    }

    private fun updateMonthDisplay() {
        binding.tvSelectedMonth.text = selectedMonthYear
    }

    private fun loadBudgetData() {
        lifecycleScope.launch {
            try {
                val goal = AppDatabase.getDatabase(this@BudgetActivity).budgetGoalDao().getGoalForMonth(selectedMonthYear)
                if (goal != null) {
                    binding.etMinGoal.setText(goal.minGoal.toString())
                    binding.etMaxGoal.setText(goal.maxGoal.toString())
                    showBudgetStatus(goal)
                } else {
                    binding.etMinGoal.text?.clear()
                    binding.etMaxGoal.text?.clear()
                    binding.cardStatus.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading budget", e)
            }
        }
    }

    private fun showBudgetStatus(goal: BudgetGoalEntity) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@BudgetActivity)
                val parts = selectedMonthYear.split("-")
                val month = parts[0].toInt() - 1
                val year = parts[1].toInt()

                val startCal = Calendar.getInstance().apply {
                    set(year, month, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val total = db.expenseDao().getTotalExpensesInPeriod(startCal.timeInMillis, endCal.timeInMillis) ?: 0.0
                val progress = ((total / goal.maxGoal) * 100).toInt().coerceIn(0, 100)

                binding.cardStatus.visibility = View.VISIBLE
                binding.tvTotalSpent.text = "Spent: R${String.format("%,.2f", total)}"
                binding.tvGoalRange.text = "Range: R${String.format("%,.2f", goal.minGoal)} - R${String.format("%,.2f", goal.maxGoal)}"
                binding.progressBudget.progress = progress
                binding.tvProgressPercent.text = "$progress%"

                binding.tvBudgetStatus.text = when {
                    total > goal.maxGoal -> "Over budget!"
                    total < goal.minGoal -> "Below minimum"
                    else -> "Within budget"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating budget status", e)
            }
        }
    }
}
