package com.codecash.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codecash.app.R
import com.codecash.app.data.entity.CategoryEntity

class CategoryAdapter(
    private var categories: List<CategoryEntity>,
    private val onDeleteClick: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val btnDeleteCategory: ImageButton = view.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvCategoryName.text = category.name
        
        holder.btnDeleteCategory.setOnClickListener {
            onDeleteClick(category)
        }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<CategoryEntity>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
