package com.codecash.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {

    private var isBalanceVisible = true
    private val balanceAmount = "R12,450.80"
    private val hiddenBalance = "•••••••"

    // Mock transaction data per OPSC DOC MO (Model)
    private val transactions = listOf(
        Transaction("1", "Starbucks", "Food & Drinks", "Mar 21, 2025", -79.99, "SB"),
        Transaction("2", "Rent", "Residence", "Mar 20, 2025", -7895.00, "RT"),
        Transaction("3", "Car", "Transportation", "Mar 19, 2025", -8526.00, "CR"),
        Transaction("4", "Salary", "Income", "Mar 15, 2025", 32960.00, "SL"),
        Transaction("5", "Grocery", "Food", "Mar 14, 2025", -1250.50, "GR")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // OPSC DOC: POE - Primary entry point after auth
        // MM: Dashboard is the mental model anchor - balance, income, expenses

        setupBalanceToggle()
        setupQuickActions()
        setupTransactionsList()
        setupBottomNavigation()
        setupProfileButton()
    }

    private fun setupBalanceToggle() {
        val btnToggle = findViewById<ImageButton>(R.id.btnToggleBalance)
        val tvBalance = findViewById<TextView>(R.id.tvBalance)

        btnToggle.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            tvBalance.text = if (isBalanceVisible) balanceAmount else hiddenBalance
            btnToggle.setImageResource(
                if (isBalanceVisible) R.drawable.ic_eye else R.drawable.ic_eye_off
            )
        }
    }

    private fun setupQuickActions() {
        findViewById<MaterialButton>(R.id.btnSend).setOnClickListener {
            startActivity(Intent(this, SendMoneyActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnRequest).setOnClickListener {
            startActivity(Intent(this, RequestMoneyActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnTopUp).setOnClickListener {
            startActivity(Intent(this, TopUpActivity::class.java))
        }
    }

    private fun setupTransactionsList() {
        val rvTransactions = findViewById<RecyclerView>(R.id.rvTransactions)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = TransactionAdapter(transactions) { transaction ->
            // Navigate to transaction detail
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("transaction", transaction)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
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
                R.id.nav_forecast -> {
                    startActivity(Intent(this, ForecastActivity::class.java))
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

    private fun setupProfileButton() {
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun showFeatureToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}