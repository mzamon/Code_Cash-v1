package com.codecash.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SendMoneyActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_money)
        
        val etRecipient = findViewById<TextInputEditText>(R.id.etRecipient)
        val etAmount = findViewById<TextInputEditText>(R.id.etSendAmount)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSendSubmit)
        val btnCancel = findViewById<MaterialButton>(R.id.btnSendCancel)
        
        btnSubmit.setOnClickListener {
            val recipient = etRecipient.text.toString().trim()
            val amount = etAmount.text.toString().trim()
            
            if (recipient.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Toast.makeText(this, "✅ Sent R$amount to $recipient (Demo – No actual transfer)", Toast.LENGTH_LONG).show()
            finish()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }
}
