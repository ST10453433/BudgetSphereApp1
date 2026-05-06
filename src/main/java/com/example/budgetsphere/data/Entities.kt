package com.example.budgetsphere.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// ---- USER ----
// fullName = display name shown on dashboard ("Welcome, John!")
// username = used for login
// password = used for login check
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,       // e.g. "John Smith"
    val username: String,       // e.g. "john_smith"
    val password: String        // e.g. "pass1234"
)

// ---- CATEGORY ----
// name     = category label e.g. "Groceries"
// colorHex = color for UI e.g. "#1D9E75"
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val colorHex: String = "#1D9E75"
)

// ---- EXPENSE ----
// Links to a Category via categoryId (foreign key)
// photoPath is nullable — photo is always optional
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity      = Category::class,
            parentColumns = ["id"],
            childColumns  = ["categoryId"],
            onDelete    = ForeignKey.CASCADE   // if category deleted, expenses deleted too
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,           // "yyyy-MM-dd"  e.g. "2026-04-01"
    val startTime: String,      // "HH:mm"       e.g. "09:00"
    val endTime: String,        // "HH:mm"       e.g. "09:30"
    val description: String,    // e.g. "Weekly grocery run"
    val amount: Double,         // e.g. 350.00
    val categoryId: Long,        // links to Category.id
    val photoPath: String? = null  // null if no photo taken
)

// ---- BUDGET GOAL ----
// Stores the user's min and max monthly spending goal
@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val minGoal: Double,        // minimum goal e.g. 1000.0
    val maxGoal: Double         // maximum goal e.g. 5000.0
)