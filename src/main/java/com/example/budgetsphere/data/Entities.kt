package com.example.budgetsphere.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── User ──────────────────────────────────────────────────────
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String
)

// ── Category ──────────────────────────────────────────────────
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

// ── Expense ───────────────────────────────────────────────────
// imagePath stores the absolute file path to the receipt photo.
// It is nullable — expenses without a receipt just have null here.
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title:      String,
    val amount:     Double,
    val date:       String,
    val categoryId: Int,
    val note:       String? = null,
    val imagePath:  String? = null   // <-- this field is REQUIRED for camera/gallery to work
)

// ── Budget Goal ───────────────────────────────────────────────
@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val minGoal: Double,
    val maxGoal: Double,
    val createdAt: String
)