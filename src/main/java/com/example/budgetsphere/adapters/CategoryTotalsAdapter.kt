package com.example.budgetsphere.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetsphere.R

class CategoryTotalsAdapter :
    RecyclerView.Adapter<CategoryTotalsAdapter.ViewHolder>() {

    private var items: List<Pair<String, Double>> = emptyList()

    fun submitData(data: List<Pair<String, Double>>) {
        items = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, total) = items[position]
        val grandTotal    = items.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
        val percent       = ((total / grandTotal) * 100).toInt()

        holder.tvCategoryName.text  = name
        holder.tvCategoryTotal.text = "R %.2f".format(total)
        holder.tvPercent.text       = "$percent%"
        holder.progressBar.progress = percent

        val colour = when (name.lowercase()) {
            "groceries"     -> "#1AAFAA"
            "transport"     -> "#1565C0"
            "entertainment" -> "#6A1B9A"
            "utilities"     -> "#F57C00"
            else            -> "#9E9E9E"
        }
        holder.tvCategoryName.setTextColor(Color.parseColor(colour))
        holder.progressBar.progressTintList = ColorStateList.valueOf(Color.parseColor(colour))
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryName:  TextView    = view.findViewById(R.id.tvCategoryName)
        val tvCategoryTotal: TextView    = view.findViewById(R.id.tvCategoryTotal)
        val tvPercent:       TextView    = view.findViewById(R.id.tvPercent)
        val progressBar:     ProgressBar = view.findViewById(R.id.progressBarCategory)
    }
}