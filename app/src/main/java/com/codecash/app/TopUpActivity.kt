package com.codecash.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class TopUpActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)
        
        val etAmount = findViewById<TextInputEditText>(R.id.etTopUpAmount)
        val etPaymentMethod = findViewById<TextInputEditText>(R.id.etPaymentMethod)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnTopUpSubmit)
        val btnCancel = findViewById<MaterialButton>(R.id.btnTopUpCancel)
        
        btnSubmit.setOnClickListener {
            val amount = etAmount.text.toString().trim()
            val method = etPaymentMethod.text.toString().trim()
            
            if (amount.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Toast.makeText(this, "🔋 Topped up R$amount from ${method.ifEmpty { "Bank Account" }} (Demo)", Toast.LENGTH_LONG).show()
            finish()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }
}
