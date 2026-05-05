package com.example.budgetsphere.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// --- User entity ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String
)

// --- Category entity ---
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String = "#1D9E75"
)

// --- Expense entity ---
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,          // stored as "yyyy-MM-dd"
    val startTime: String,     // "HH:mm"
    val endTime: String,       // "HH:mm"
    val description: String,
    val amount: Double,
    val categoryId: Long,      // Matches Category ID type
    val photoPath: String? = null
)

// --- BudgetGoal entity ---
@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val minGoal: Double,
    val maxGoal: Double
)