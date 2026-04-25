package com.codecash.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RequestMoneyActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_money)
        
        val etRequestFrom = findViewById<TextInputEditText>(R.id.etRequestFrom)
        val etAmount = findViewById<TextInputEditText>(R.id.etRequestAmount)
        val etReason = findViewById<TextInputEditText>(R.id.etReason)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnRequestSubmit)
        val btnCancel = findViewById<MaterialButton>(R.id.btnRequestCancel)
        
        btnSubmit.setOnClickListener {
            val from = etRequestFrom.text.toString().trim()
            val amount = etAmount.text.toString().trim()
            
            if (from.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Toast.makeText(this, "💰 Requested R$amount from $from (Demo)", Toast.LENGTH_LONG).show()
            finish()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }
}
