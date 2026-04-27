package com.codecash.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.BudgetGoal
import com.codecash.app.databinding.ActivityBudgetGoalBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class BudgetGoalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetGoalBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private val monthNames = listOf("January","February","March","April","May","June",
        "July","August","September","October","November","December")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }
        updateMonthDisplay()
        loadGoal()

        binding.btnPreviousMonth.setOnClickListener {
            if (selectedMonth == 1) { selectedMonth = 12; selectedYear-- } else selectedMonth--
            updateMonthDisplay(); loadGoal()
        }
        binding.btnNextMonth.setOnClickListener {
            if (selectedMonth == 12) { selectedMonth = 1; selectedYear++ } else selectedMonth++
            updateMonthDisplay(); loadGoal()
        }
        binding.btnSaveGoal.setOnClickListener { validateAndSave() }
    }

    private fun updateMonthDisplay() {
        binding.tvMonthYear.text = "${monthNames[selectedMonth - 1]} $selectedYear"
    }

    private fun loadGoal() {
        lifecycleScope.launch {
            val goal = db.budgetGoalDao().getGoalForMonth(session.getUserId(), selectedMonth, selectedYear)
            if (goal != null) {
                binding.etMinAmount.setText(String.format("%.2f", goal.minAmount))
                binding.etMaxAmount.setText(String.format("%.2f", goal.maxAmount))
                binding.tvCurrentGoal.text =
                    "Current: ${currencyFormat.format(goal.minAmount)} – ${currencyFormat.format(goal.maxAmount)}"
            } else {
                binding.etMinAmount.text?.clear()
                binding.etMaxAmount.text?.clear()
                binding.tvCurrentGoal.text = "No goal set for this month"
            }
        }
    }

    private fun validateAndSave() {
        val minStr = binding.etMinAmount.text.toString().trim()
        val maxStr = binding.etMaxAmount.text.toString().trim()

        if (minStr.isEmpty()) { binding.tilMinAmount.error = "Required"; return }
        else binding.tilMinAmount.error = null
        if (maxStr.isEmpty()) { binding.tilMaxAmount.error = "Required"; return }
        else binding.tilMaxAmount.error = null

        val min = minStr.toDoubleOrNull()
        val max = maxStr.toDoubleOrNull()

        if (min == null || min < 0) { binding.tilMinAmount.error = "Invalid amount"; return }
        if (max == null || max < 0) { binding.tilMaxAmount.error = "Invalid amount"; return }
        if (min >= max) { binding.tilMaxAmount.error = "Maximum must be greater than minimum"; return }

        lifecycleScope.launch {
            db.budgetGoalDao().insertGoal(BudgetGoal(
                userId = session.getUserId(),
                month = selectedMonth,
                year = selectedYear,
                minAmount = min,
                maxAmount = max
            ))
            binding.tvCurrentGoal.text =
                "Current: ${currencyFormat.format(min)} – ${currencyFormat.format(max)}"
            Toast.makeText(this@BudgetGoalActivity, "Goal saved", Toast.LENGTH_SHORT).show()
        }
    }
}
