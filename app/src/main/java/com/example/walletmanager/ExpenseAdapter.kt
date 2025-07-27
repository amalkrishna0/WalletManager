package com.example.walletmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.walletapp.Expense
import com.example.walletmanager.databinding.ExpenseItemBinding

class ExpenseAdapter(private var expenses: List<Expense> , private val onLongPress: (Expense) -> Unit) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.itemView.setOnClickListener {
            onLongPress(expense)
        }

        holder.binding.textViewAmount.text = "₹ ${expense.amount ?: "0.00"}"
        holder.binding.textViewDescription.text = expense.description ?: "No Description"

        val dateParts = expense.date?.split(" ")
        val dateOnly = dateParts?.getOrNull(0) ?: "N/A"
        val timeOnly = expense.time

        holder.binding.textViewDate.text = dateOnly
        holder.binding.textViewTime.text = timeOnly
    }


    override fun getItemCount(): Int = expenses.size

    // 🔁 Add this to support filtering
    fun updateList(newList: List<Expense>) {
        expenses = newList
        notifyDataSetChanged()
    }
}
