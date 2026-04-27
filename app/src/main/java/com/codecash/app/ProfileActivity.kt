package com.codecash.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityProfileBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        db = AppDatabase.getInstance(this)

        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            val user = db.userDao().getUserById(session.getUserId())
            binding.tvDisplayName.text = user?.displayName ?: session.getDisplayName()
            binding.tvUsername.text = "@${user?.username ?: session.getUsername()}"

            val entryCount = db.expenseEntryDao()
                .getEntriesInPeriod(session.getUserId(), "0000-01-01", "9999-12-31").size
            val categoryCount = db.categoryDao()
                .getCategoriesByUserOnce(session.getUserId()).size

            binding.tvEntryCount.text = "$entryCount"
            binding.tvCategoryCount.text = "$categoryCount"
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Log Out")
                .setMessage("Are you sure?")
                .setPositiveButton("Log Out") { _, _ ->
                    session.clearSession()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
