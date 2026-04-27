package com.codecash.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.ExpenseAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.Category
import com.codecash.app.databinding.ActivityDashboardBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var expenseAdapter: ExpenseAdapter
    private var categoryMap = mapOf<Long, Category>()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)

        if (!session.isLoggedIn()) { navigateToLogin(); return }

        binding.tvWelcome.text = "Welcome, ${session.getDisplayName()} 👋"

        setupRecyclerView()
        loadCategories()
        observeRecentExpenses()
        loadBudgetSummary()
        setupNavigation()
    }

    private fun setupNavigation() {
        binding.btnAddExpense.setOnClickListener { startActivity(Intent(this, AddExpenseActivity::class.java)) }
        binding.btnCategories.setOnClickListener { startActivity(Intent(this, CategoryActivity::class.java)) }
        binding.btnExpenseList.setOnClickListener { startActivity(Intent(this, ExpenseListActivity::class.java)) }
        binding.btnCategoryTotals.setOnClickListener { startActivity(Intent(this, CategoryTotalsActivity::class.java)) }
        binding.btnBudgetGoal.setOnClickListener { startActivity(Intent(this, BudgetGoalActivity::class.java)) }
        binding.btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvViewAll.setOnClickListener { startActivity(Intent(this, ExpenseListActivity::class.java)) }
        binding.btnLogout.setOnClickListener {
            session.clearSession()
            navigateToLogin()
        }
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(emptyList(), categoryMap) { entry ->
            val intent = Intent(this, ExpenseDetailActivity::class.java)
            intent.putExtra("entry_id", entry.id)
            startActivity(intent)
        }
        binding.rvRecentExpenses.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = expenseAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            db.categoryDao().getCategoriesByUser(session.getUserId()).collectLatest { cats ->
                categoryMap = cats.associateBy { it.id }
                expenseAdapter.updateCategories(categoryMap)
            }
        }
    }

    private fun observeRecentExpenses() {
        lifecycleScope.launch {
            db.expenseEntryDao().getRecentEntries(session.getUserId()).collectLatest { entries ->
                expenseAdapter.updateData(entries)
            }
        }
    }

    private fun loadBudgetSummary() {
        lifecycleScope.launch {
            try {
                val cal = Calendar.getInstance()
                val month = cal.get(Calendar.MONTH) + 1
                val year = cal.get(Calendar.YEAR)
                val monthPattern = String.format("%04d-%02d%%", year, month)

                val totalSpent = db.expenseEntryDao()
                    .getTotalSpentInMonth(session.getUserId(), monthPattern) ?: 0.0
                val goal = db.budgetGoalDao().getGoalForMonth(session.getUserId(), month, year)

                binding.tvTotalSpent.text = currencyFormat.format(totalSpent)

                if (goal != null) {
                    binding.tvBudgetRange.text =
                        "Goal: ${currencyFormat.format(goal.minAmount)} – ${currencyFormat.format(goal.maxAmount)}"
                    val progress = when {
                        totalSpent <= goal.minAmount -> 0
                        totalSpent >= goal.maxAmount -> 100
                        else -> ((totalSpent - goal.minAmount) /
                                (goal.maxAmount - goal.minAmount) * 100).toInt()
                    }
                    binding.budgetProgressBar.progress = progress
                } else {
                    binding.tvBudgetRange.text = "No budget goal set for this month"
                    binding.budgetProgressBar.progress = 0
                }
            } catch (e: Exception) {
                // Handled silently — UI shows defaults
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadBudgetSummary()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
