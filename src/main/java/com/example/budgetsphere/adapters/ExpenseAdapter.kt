package com.example.budgetsphere.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetsphere.R
import com.example.budgetsphere.data.Category
import com.example.budgetsphere.data.Expense

class ExpenseAdapter(
    private val context: Context,
    private val onDelete: ((Expense) -> Unit)? = null
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses: List<Expense>      = emptyList()
    private var catMap:   Map<Int, Category> = emptyMap()

    fun submitData(expenses: List<Expense>, catMap: Map<Int, Category>) {
        this.expenses = expenses
        this.catMap   = catMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        val catName = catMap[expense.categoryId]?.name ?: "Uncategorised"

        holder.tvTitle.text      = expense.title
        holder.tvAmount.text     = "R %.2f".format(expense.amount)
        holder.tvDate.text       = expense.date
        holder.tvCategory.text   = catName
        holder.tvNote.text       = expense.note ?: ""
        holder.tvNote.visibility = if (expense.note.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete expense?")
                .setMessage("\"${expense.title}\" — R %.2f".format(expense.amount))
                .setPositiveButton("Delete") { _, _ -> onDelete?.invoke(expense) }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    override fun getItemCount() = expenses.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle:    TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount:   TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate:     TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvNote:     TextView = itemView.findViewById(R.id.tvNote)
    }
}