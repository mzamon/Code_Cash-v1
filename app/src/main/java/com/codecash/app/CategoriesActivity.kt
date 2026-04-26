package com.codecash.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.CategoryAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.data.entity.CategoryEntity
import com.codecash.app.databinding.ActivityCategoriesBinding
import kotlinx.coroutines.launch

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val TAG = "CategoriesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupAddButton()
        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            deleteCategory(category)
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = categoryAdapter
    }

    private fun setupAddButton() {
        binding.btnAddCategory.setOnClickListener {
            val categoryName = binding.etCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_category_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val category = CategoryEntity(name = categoryName)
                    AppDatabase.getDatabase(this@CategoriesActivity).categoryDao().insert(category)
                    Toast.makeText(this@CategoriesActivity, getString(R.string.category_added), Toast.LENGTH_SHORT).show()
                    binding.etCategoryName.text?.clear()
                    loadCategories()
                } catch (e: Exception) {
                    Log.e(TAG, "Error adding category", e)
                    Toast.makeText(this@CategoriesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteCategory(category: CategoryEntity) {
        lifecycleScope.launch {
            try {
                AppDatabase.getDatabase(this@CategoriesActivity).categoryDao().delete(category)
                Toast.makeText(this@CategoriesActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                loadCategories()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting category", e)
                Toast.makeText(this@CategoriesActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val categories = AppDatabase.getDatabase(this@CategoriesActivity).categoryDao().getAllCategoriesList()
                categoryAdapter.updateData(categories)
                
                if (categories.isEmpty()) {
                    binding.tvEmpty.visibility = android.view.View.VISIBLE
                    binding.rvCategories.visibility = android.view.View.GONE
                } else {
                    binding.tvEmpty.visibility = android.view.View.GONE
                    binding.rvCategories.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading categories", e)
            }
        }
    }
}
