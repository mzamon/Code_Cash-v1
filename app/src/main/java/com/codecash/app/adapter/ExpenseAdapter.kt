package com.codecash.app.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codecash.app.data.entity.Category
import com.codecash.app.data.entity.ExpenseEntry
import com.codecash.app.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private var entries: List<ExpenseEntry>,
    private var categoryMap: Map<Long, Category>,
    private val onItemClick: (ExpenseEntry) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))

    inner class ViewHolder(private val binding: ItemExpenseBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: ExpenseEntry) {
            val category = entry.categoryId?.let { categoryMap[it] }
            try {
                binding.viewColorStrip.setBackgroundColor(
                    Color.parseColor(category?.colorHex ?: "#8BA4C0"))
            } catch (e: Exception) {
                binding.viewColorStrip.setBackgroundColor(Color.parseColor("#8BA4C0"))
            }
            binding.tvDescription.text = entry.description
            binding.tvCategory.text = category?.name ?: "Uncategorised"
            binding.tvDate.text = entry.date
            binding.tvTime.text = "${entry.startTime} – ${entry.endTime}"
            binding.tvAmount.text = currencyFormat.format(entry.amount)
            binding.ivPhotoIndicator.visibility =
                if (!entry.photoPath.isNullOrEmpty()) android.view.View.VISIBLE
                else android.view.View.GONE
            binding.root.setOnClickListener { onItemClick(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(entries[position])
    override fun getItemCount() = entries.size

    fun updateData(newEntries: List<ExpenseEntry>) { entries = newEntries; notifyDataSetChanged() }
    fun updateCategories(newMap: Map<Long, Category>) { categoryMap = newMap; notifyDataSetChanged() }
}
