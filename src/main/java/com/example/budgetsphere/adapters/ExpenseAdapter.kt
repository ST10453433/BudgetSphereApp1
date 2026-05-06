package com.example.budgetsphere.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.budgetsphere.R
import com.example.budgetsphere.data.Category
import com.example.budgetsphere.data.Expense
import java.io.File

class ExpenseAdapter(private val context: Context) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private var expenses: List<Expense> = emptyList()
    private var categoryMap: Map<Int, Category> = emptyMap()

    fun submitData(expenses: List<Expense>, categoryMap: Map<Int, Category>) {
        this.expenses = expenses
        this.categoryMap = categoryMap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        // Fix: Explicitly convert the ID to Int to match the Map key type
        // This avoids the "Incompatible types" error.
        val categoryId = expense.categoryId.toInt()
        val category = categoryMap[categoryId]

        holder.tvDescription.text = expense.description
        holder.tvAmount.text = "R %.2f".format(expense.amount)
        holder.tvDate.text = "${expense.date}  ${expense.startTime} – ${expense.endTime}"
        holder.tvCategory.text = category?.name ?: "Unknown"

        // Handle Image loading logic
        if (!expense.photoPath.isNullOrEmpty()) {
            val file = File(expense.photoPath!!)
            if (file.exists()) {
                holder.ivPhoto.visibility = View.VISIBLE
                Glide.with(context)
                    .load(file)
                    .centerCrop()
                    .into(holder.ivPhoto)
            } else {
                holder.ivPhoto.visibility = View.GONE
            }
        } else {
            holder.ivPhoto.visibility = View.GONE
        }
    }

    override fun getItemCount() = expenses.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAmount: TextView      = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView        = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView    = itemView.findViewById(R.id.tvCategory)
        // Fix: Removed the trailing "." that was causing a syntax error
        val ivPhoto: ImageView      = itemView.findViewById(R.id.ivPhoto)
    }
}