package com.example.budgetsphere.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log

// Increment version number any time you change an entity
@Database(
    entities = [User::class, Category::class, Expense::class, BudgetGoal::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        private const val TAG = "AppDatabase"

        // Volatile ensures all threads see the same instance
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            Log.d(TAG, "Getting database instance")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetsphere_db"
                )
                    .fallbackToDestructiveMigration()  // Drops & recreates on version change
                    .build()
                INSTANCE = instance
                Log.d(TAG, "Database instance created")
                instance
            }
        }
    }
}