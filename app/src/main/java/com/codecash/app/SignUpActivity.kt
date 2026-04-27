package com.codecash.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.User
import com.codecash.app.databinding.ActivitySignupBinding
import com.codecash.app.util.HashUtils
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)

        binding.btnCreateAccount.setOnClickListener { attemptRegistration() }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun attemptRegistration() {
        val displayName = binding.etDisplayName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        var isValid = true
        if (displayName.isEmpty()) { binding.tilDisplayName.error = "Name is required"; isValid = false }
        else binding.tilDisplayName.error = null

        if (username.isEmpty()) { binding.tilUsername.error = "Username is required"; isValid = false }
        else if (username.length < 3) { binding.tilUsername.error = "At least 3 characters"; isValid = false }
        else binding.tilUsername.error = null

        if (password.isEmpty()) { binding.tilPassword.error = "Password is required"; isValid = false }
        else if (password.length < 6) { binding.tilPassword.error = "At least 6 characters"; isValid = false }
        else binding.tilPassword.error = null

        if (confirmPassword != password) { binding.tilConfirmPassword.error = "Passwords do not match"; isValid = false }
        else binding.tilConfirmPassword.error = null

        if (!isValid) return

        setLoading(true)

        lifecycleScope.launch {
            try {
                val existing = db.userDao().getUserByUsername(username)
                if (existing != null) {
                    setLoading(false)
                    binding.tilUsername.error = "Username already taken"
                    return@launch
                }

                val user = User(
                    username = username,
                    passwordHash = HashUtils.sha256(password),
                    displayName = displayName
                )
                val userId = db.userDao().insertUser(user)
                session.saveSession(userId, username, displayName)
                startActivity(Intent(this@SignUpActivity, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@SignUpActivity,
                    "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateAccount.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
