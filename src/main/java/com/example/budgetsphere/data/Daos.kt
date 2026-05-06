package com.example.budgetsphere.data

import androidx.lifecycle.LiveData
import androidx.room.*

// ============================================================
// USER DAO
// ============================================================
@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: User): Long

    // Login — match both username AND password
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // Check if username already taken during registration
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?
}

// ============================================================
// CATEGORY DAO
// ============================================================
@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun delete(category: Category)

    // Returns LiveData — use in fragments that observe changes
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    // Returns plain list — use inside coroutines (no observer needed)
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesOnce(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Category?
}

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    // For date-filtered history screen — returns plain list safely
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesBetweenOnce(startDate: String, endDate: String): List<Expense>

    // For dashboard — all expenses this month
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesOnce(): List<Expense>

    // For observing live changes in real time
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    // Category totals for a date range — used in CategoryTotalsFragment
    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY categoryId
    """)
    suspend fun getCategoryTotals(startDate: String, endDate: String): List<CategoryTotal>
}

// Helper class for the category totals query result
data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)

// ============================================================
// BUDGET GOAL DAO
// ============================================================
@Dao
interface BudgetGoalDao {

    // REPLACE means if a goal exists it gets overwritten (only one goal at a time)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(goal: BudgetGoal)

    // Get the most recently saved goal
    @Query("SELECT * FROM budget_goals ORDER BY id DESC LIMIT 1")
    suspend fun getLatestGoal(): BudgetGoal?

    // Live version for dashboard observation
    @Query("SELECT * FROM budget_goals ORDER BY id DESC LIMIT 1")
    fun getLatestGoalLive(): LiveData<BudgetGoal?>
}
