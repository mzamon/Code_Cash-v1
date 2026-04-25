package com.codecash.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // OPSC DOC: POE - Entry point of application
        // MM: Mental model - User sees brand identity first

        findViewById<MaterialButton>(R.id.btnGetStarted).setOnClickListener {
            // Navigate to Sign Up for new users
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            // Navigate to Login for existing users
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}