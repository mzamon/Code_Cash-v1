package com.codecash.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codecash.app.R
import com.codecash.app.data.entity.ExpenseEntity
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<ExpenseEntity>,
    private var categoryMap: Map<Int, String>,
    private val onItemClick: (ExpenseEntity) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val ivPhotoIndicator: ImageView = view.findViewById(R.id.ivPhotoIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvDescription.text = expense.description
        holder.tvCategory.text = categoryMap[expense.categoryId] ?: "Unknown"
        holder.tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.date))
        holder.tvAmount.text = "R${String.format("%,.2f", expense.amount)}"
        holder.tvAmount.setTextColor(holder.itemView.context.getColor(R.color.expense_red))
        
        holder.ivPhotoIndicator.visibility = if (expense.photoPath != null) View.VISIBLE else View.GONE
        
        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<ExpenseEntity>, newCategoryMap: Map<Int, String>) {
        expenses = newExpenses
        categoryMap = newCategoryMap
        notifyDataSetChanged()
    }
}
