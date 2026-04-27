package com.codecash.app.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecash.app.data.entity.Category
import com.codecash.app.databinding.ItemCategoryBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(
    private var categories: List<Category>,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class ViewHolder(private val binding: ItemCategoryBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            
            if (category.monthlyLimit > 0) {
                binding.tvCategoryLimit.visibility = View.VISIBLE
                binding.tvCategoryLimit.text = "Limit: ${currencyFormat.format(category.monthlyLimit)}"
            } else {
                binding.tvCategoryLimit.visibility = View.GONE
            }

            try {
                binding.viewColor.setBackgroundColor(Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                binding.viewColor.setBackgroundColor(Color.parseColor("#00D4AA"))
            }
            binding.btnDelete.setOnClickListener { onDeleteClick(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(categories[position])
    override fun getItemCount() = categories.size
    fun updateData(newList: List<Category>) { categories = newList; notifyDataSetChanged() }
}
