package com.codecash.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.ExpenseAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupButtons()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(emptyList(), emptyMap()) { expense ->
            val intent = Intent(this, ExpenseDetailActivity::class.java)
            intent.putExtra("expense_id", expense.id)
            startActivity(intent)
        }
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvRecentTransactions.adapter = expenseAdapter
    }

    private fun setupButtons() {
        binding.btnAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        binding.btnViewAll.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }
        binding.btnCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
        binding.btnBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatsActivity::class.java))
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@DashboardActivity)
                val cal = Calendar.getInstance()
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)

                val startCal = Calendar.getInstance().apply {
                    set(year, month, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val expenses = db.expenseDao().getExpensesInPeriod(startCal.timeInMillis, endCal.timeInMillis)
                val totalExpenses = expenses.sumOf { it.amount }
                val categories = db.categoryDao().getAllCategoriesList()
                val catMap = categories.associateBy({ it.id }, { it.name })

                binding.tvTotalExpenses.text = "R${String.format("%,.2f", totalExpenses)}"

                val monthYearStr = SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(cal.time)
                val goal = db.budgetGoalDao().getGoalForMonth(monthYearStr)

                if (goal != null) {
                    val progress = ((totalExpenses / goal.maxGoal) * 100).toInt().coerceIn(0, 100)
                    binding.progressBudget.progress = progress
                    binding.tvBudgetStatus.text = when {
                        totalExpenses > goal.maxGoal -> "Over budget! Max: R${String.format("%,.2f", goal.maxGoal)}"
                        totalExpenses < goal.minGoal -> "Below minimum: R${String.format("%,.2f", goal.minGoal)}"
                        else -> "Within budget range"
                    }
                    val colorRes = when {
                        totalExpenses > goal.maxGoal -> R.color.expense_red
                        totalExpenses < goal.minGoal -> R.color.warning_amber
                        else -> R.color.income_green
                    }
                    binding.tvBudgetStatus.setTextColor(getColor(colorRes))
                } else {
                    binding.tvBudgetStatus.text = "No budget goal set"
                    binding.progressBudget.progress = 0
                }

                expenseAdapter.updateData(expenses.take(5), catMap)
                Log.d(TAG, "Loaded ${expenses.size} expenses")
            } catch (e: Exception) {
                Log.e(TAG, "Dashboard load error", e)
            }
        }
    }
}