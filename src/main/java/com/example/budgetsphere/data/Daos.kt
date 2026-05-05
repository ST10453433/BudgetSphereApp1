package com.example.budgetsphere.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?
}

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesOnce(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Category?
}

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getExpensesBetweenOnce(startDate: String, endDate: String): List<Expense>

    @Query("""
        SELECT date, SUM(amount) as total 
        FROM expenses 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY date 
        ORDER BY date ASC
    """)
    suspend fun getDailyTotals(startDate: String, endDate: String): List<DailyTotal>

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM expenses 
        WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY categoryId
    """)
    suspend fun getCategoryTotals(startDate: String, endDate: String): List<CategoryTotal>
}

// --- Query Result Helper Classes ---
data class CategoryTotal(
    val categoryId: Long, // Matches Entity ID
    val total: Double
)

data class DailyTotal(
    val date: String,
    val total: Double
)

@Dao
interface BudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(goal: BudgetGoal)

    @Query("SELECT * FROM budget_goals ORDER BY id DESC LIMIT 1")
    suspend fun getLatestGoal(): BudgetGoal?

    @Query("SELECT * FROM budget_goals ORDER BY id DESC LIMIT 1")
    fun getLatestGoalLive(): LiveData<BudgetGoal?>
}