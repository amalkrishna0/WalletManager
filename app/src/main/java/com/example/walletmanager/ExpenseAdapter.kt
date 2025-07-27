package com.example.walletmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walletapp.Expense
import com.example.walletmanager.databinding.ExpenseItemBinding

class ExpenseAdapter(private val expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.binding.textViewAmount.text = "₹ ${expense.amount ?: "0.00"}"
        holder.binding.textViewDescription.text = expense.description ?: "No Description"
        holder.binding.textViewDate.text = expense.date ?: "N/A"
    }

    override fun getItemCount(): Int = expenses.size
}
