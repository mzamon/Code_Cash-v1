package com.codecash.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMerchant: TextView = view.findViewById(R.id.tvMerchant)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvMerchant.text = transaction.merchantName
        holder.tvCategory.text = transaction.category
        
        val amountText = if (transaction.amount < 0) {
            "-R${String.format("%,.2f", kotlin.math.abs(transaction.amount))}"
        } else {
            "+R${String.format("%,.2f", transaction.amount)}"
        }
        
        holder.tvAmount.text = amountText
        holder.tvAmount.setTextColor(
            if (transaction.amount < 0) 
                holder.itemView.context.getColor(R.color.expense_red)
            else 
                holder.itemView.context.getColor(R.color.income_green)
        )
        
        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }
    
    override fun getItemCount() = transactions.size
}
