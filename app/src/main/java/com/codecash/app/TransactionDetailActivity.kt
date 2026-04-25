package com.codecash.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TransactionDetailActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)
        
        val transaction = intent.getSerializableExtra("transaction") as? Transaction
        
        val tvMerchant = findViewById<TextView>(R.id.tvDetailMerchant)
        val tvCategory = findViewById<TextView>(R.id.tvDetailCategory)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvAmount = findViewById<TextView>(R.id.tvDetailAmount)
        val tvReference = findViewById<TextView>(R.id.tvDetailReference)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        
        transaction?.let {
            tvMerchant.text = it.merchantName
            tvCategory.text = it.category
            tvDate.text = it.date
            
            val amountText = if (it.amount < 0) {
                "-R${String.format("%,.2f", kotlin.math.abs(it.amount))}"
            } else {
                "+R${String.format("%,.2f", it.amount)}"
            }
            tvAmount.text = amountText
            tvAmount.setTextColor(
                if (it.amount < 0) getColor(R.color.expense_red) else getColor(R.color.income_green)
            )
            
            tvReference.text = "TXN-${it.id}-2025-${it.iconInitials}"
        }
        
        btnBack.setOnClickListener {
            finish()
        }
    }
}
