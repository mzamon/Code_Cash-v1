package com.codecash.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StatsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        
        Toast.makeText(this, "📈 Statistics & Analytics – Coming in Part 2!", Toast.LENGTH_LONG).show()
        
        findViewById<ImageButton>(R.id.btnStatsBack).setOnClickListener {
            finish()
        }
    }
}
