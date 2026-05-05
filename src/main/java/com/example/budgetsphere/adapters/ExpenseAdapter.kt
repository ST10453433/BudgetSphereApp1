package com.example.budgetsphere.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetsphere.R
import com.example.budgetsphere.data.Category
import com.example.budgetsphere.data.Expense

class ExpenseAdapter(requireContext: Context) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private var expenses: List<Expense> = emptyList()
    private var categoryMap: Map<Long, Category> = emptyMap()

    fun submitData(expenses: List<Expense>, categoryMap: Map<Long, Category>) {
        this.expenses = expenses
        this.categoryMap = categoryMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        val category = categoryMap[expense.categoryId]

        holder.tvDescription.text = expense.description
        holder.tvAmount.text = "R %.2f".format(expense.amount)
        holder.tvDate.text = expense.date
        holder.tvTime.text = "${expense.startTime} – ${expense.endTime}"

        // Display category name or fallback
        holder.tvCategory.text = category?.name ?: "Unknown"

        // Dynamically set color based on category data
        val colorString = category?.colorHex ?: "#9E9E9E"
        try {
            holder.tvCategory.setTextColor(Color.parseColor(colorString))
        } catch (e: Exception) {
            holder.tvCategory.setTextColor(Color.GRAY)
        }
    }

    override fun getItemCount(): Int = expenses.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvExpenseDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvExpenseAmount)
        val tvDate: TextView = view.findViewById(R.id.tvExpenseDate)
        val tvTime: TextView = view.findViewById(R.id.tvExpenseTime)
        val tvCategory: TextView = view.findViewById(R.id.tvExpenseCategory)
    }
}