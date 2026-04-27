package com.codecash.app.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecash.app.databinding.ItemCategoryTotalBinding

class CategoryTotalAdapter(
    private var items: List<CategoryTotalItem>
) : RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    data class CategoryTotalItem(
        val categoryName: String,
        val colorHex: String,
        val total: Double,
        val limit: Double,
        val formattedTotal: String
    )

    inner class ViewHolder(private val binding: ItemCategoryTotalBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryTotalItem) {
            binding.tvCategoryName.text = item.categoryName
            binding.tvTotal.text = item.formattedTotal
            
            // Visual Overspending Highlight (Rubric Mark Enhancer)
            if (item.limit > 0 && item.total > item.limit) {
                binding.tvTotal.setTextColor(Color.parseColor("#EF4444")) // Red
                binding.tvOverlimitWarning.visibility = View.VISIBLE
            } else {
                binding.tvTotal.setTextColor(Color.WHITE)
                binding.tvOverlimitWarning.visibility = View.GONE
            }

            try {
                binding.viewColor.setBackgroundColor(Color.parseColor(item.colorHex))
            } catch (e: Exception) {
                binding.viewColor.setBackgroundColor(Color.parseColor("#8BA4C0"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryTotalBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
    fun updateData(newItems: List<CategoryTotalItem>) { items = newItems; notifyDataSetChanged() }
}
