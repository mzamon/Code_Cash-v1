package com.codecash.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BudgetActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        
        Toast.makeText(this, getString(R.string.budget_coming_soon), Toast.LENGTH_LONG).show()
        
        findViewById<ImageButton>(R.id.btnBudgetBack).setOnClickListener {
            finish()
        }
    }
}
