package com.codecash.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForecastActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        
        Toast.makeText(this, "🔮 Financial Forecasting – Coming in Part 3!", Toast.LENGTH_LONG).show()
        
        findViewById<ImageButton>(R.id.btnForecastBack).setOnClickListener {
            finish()
        }
    }
}
