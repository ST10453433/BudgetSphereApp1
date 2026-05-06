package com.example.budgetsphere.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities  = [User::class, Category::class, Expense::class, BudgetGoal::class],
    version   = 2,          // CHANGED from 1 to 2 (fullName added to User)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetsphere_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                Log.d(TAG, "Database instance created (version 2)")
                instance
            }
        }
    }
}