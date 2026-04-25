package com.codecash.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.UserEntity
import com.codecash.app.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            when {
                name.isEmpty() || username.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    Toast.makeText(this, getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getDatabase(this@SignUpActivity)
                    if (db.userDao().getUserByUsername(username) != null) {
                        Toast.makeText(this@SignUpActivity, getString(R.string.username_exists), Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val user = UserEntity(username = username, password = password, email = "$username@codecash.local")
                    db.userDao().insert(user)
                    Log.d(TAG, "Account created: $username")
                    Toast.makeText(this@SignUpActivity, "Account created!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Signup error", e)
                    Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}