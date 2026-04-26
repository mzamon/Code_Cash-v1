package com.codecash.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    logout()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage(getString(R.string.clear_data_confirm))
                .setPositiveButton("Clear") { _, _ ->
                    clearAllData()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun logout() {
        Toast.makeText(this, getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun clearAllData() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@SettingsActivity)
                db.clearAllTables()
                Toast.makeText(this@SettingsActivity, getString(R.string.data_cleared), Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SettingsActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
