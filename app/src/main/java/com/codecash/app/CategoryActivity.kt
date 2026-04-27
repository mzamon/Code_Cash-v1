package com.codecash.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.CategoryAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.Category
import com.codecash.app.databinding.ActivityCategoryBinding
import com.codecash.app.databinding.DialogAddCategoryBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var adapter: CategoryAdapter

    private val colorOptions = listOf(
        "#00D4AA", "#4A90E2", "#FF6B6B", "#F5A623",
        "#9B59B6", "#2ECC71", "#E74C3C", "#3498DB"
    )
    private var selectedColor = "#00D4AA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        adapter = CategoryAdapter(emptyList()) { showDeleteDialog(it) }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@CategoryActivity)
            adapter = this@CategoryActivity.adapter
        }

        lifecycleScope.launch {
            db.categoryDao().getCategoriesByUser(session.getUserId()).collectLatest { cats ->
                adapter.updateData(cats)
                binding.tvEmpty.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabAddCategory.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(LayoutInflater.from(this))
        selectedColor = colorOptions[0]

        val chips = listOf(
            dialogBinding.chip1, dialogBinding.chip2, dialogBinding.chip3, dialogBinding.chip4,
            dialogBinding.chip5, dialogBinding.chip6, dialogBinding.chip7, dialogBinding.chip8
        )
        chips.forEachIndexed { i, chip ->
            if (i < colorOptions.size) {
                chip.setBackgroundColor(android.graphics.Color.parseColor(colorOptions[i]))
                chip.setOnClickListener {
                    selectedColor = colorOptions[i]
                    chips.forEach { it.alpha = 0.4f }
                    chip.alpha = 1.0f
                }
            }
        }
        chips.firstOrNull()?.alpha = 1.0f
        chips.drop(1).forEach { it.alpha = 0.4f }

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("New Category")
            .setView(dialogBinding.root)
            .setPositiveButton("Create") { _, _ ->
                val name = dialogBinding.etCategoryName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    db.categoryDao().insertCategory(
                        Category(userId = session.getUserId(), name = name, colorHex = selectedColor)
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(category: Category) {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Delete Category")
            .setMessage("Delete '${category.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch { db.categoryDao().deleteCategory(category) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
