package com.codecash.app

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityExpenseDetailBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseDetailBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }

        val entryId = intent.getLongExtra("entry_id", -1L)
        if (entryId == -1L) { finish(); return }

        lifecycleScope.launch {
            val entry = db.expenseEntryDao().getEntryById(entryId)
            if (entry == null) { finish(); return@launch }

            binding.tvDescription.text = entry.description
            binding.tvDate.text = entry.date
            binding.tvStartTime.text = entry.startTime
            binding.tvEndTime.text = entry.endTime
            binding.tvAmount.text = currencyFormat.format(entry.amount)

            val cat = entry.categoryId?.let { db.categoryDao().getCategoryById(it) }
            binding.tvCategory.text = cat?.name ?: "Uncategorised"
            cat?.let {
                try {
                    binding.viewCategoryColor.setBackgroundColor(
                        android.graphics.Color.parseColor(it.colorHex)
                    )
                } catch (e: Exception) { /* ignore */ }
            }

            if (!entry.photoPath.isNullOrEmpty()) {
                val file = File(entry.photoPath)
                if (file.exists()) {
                    binding.ivPhoto.setImageURI(Uri.fromFile(file))
                    binding.ivPhoto.visibility = View.VISIBLE
                    binding.tvNoPhoto.visibility = View.GONE
                } else {
                    binding.ivPhoto.visibility = View.GONE
                    binding.tvNoPhoto.visibility = View.VISIBLE
                }
            } else {
                binding.ivPhoto.visibility = View.GONE
                binding.tvNoPhoto.visibility = View.VISIBLE
            }

            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(this@ExpenseDetailActivity, R.style.AlertDialogTheme)
                    .setTitle("Delete Entry")
                    .setMessage("Delete this expense?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            db.expenseEntryDao().deleteEntry(entry)
                            finish()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}
