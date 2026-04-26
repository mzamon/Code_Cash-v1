package com.codecash.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codecash.app.R

data class CategoryStat(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Int
)

class CategoryStatAdapter(
    private var stats: List<CategoryStat>
) : RecyclerView.Adapter<CategoryStatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatCategory: TextView = view.findViewById(R.id.tvStatCategory)
        val tvStatAmount: TextView = view.findViewById(R.id.tvStatAmount)
        val progressStat: ProgressBar = view.findViewById(R.id.progressStat)
        val tvStatPercent: TextView = view.findViewById(R.id.tvStatPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = stats[position]
        holder.tvStatCategory.text = stat.categoryName
        holder.tvStatAmount.text = "R${String.format("%,.2f", stat.totalAmount)}"
        holder.progressStat.progress = stat.percentage
        holder.tvStatPercent.text = "${stat.percentage}%"
    }

    override fun getItemCount() = stats.size

    fun updateData(newStats: List<CategoryStat>) {
        stats = newStats
        notifyDataSetChanged()
    }
}
