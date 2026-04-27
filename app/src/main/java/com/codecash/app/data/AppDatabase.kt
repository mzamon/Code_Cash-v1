package com.codecash.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.codecash.app.data.dao.BudgetGoalDao
import com.codecash.app.data.dao.CategoryDao
import com.codecash.app.data.dao.ExpenseEntryDao
import com.codecash.app.data.dao.UserDao
import com.codecash.app.data.entity.BudgetGoal
import com.codecash.app.data.entity.Category
import com.codecash.app.data.entity.ExpenseEntry
import com.codecash.app.data.entity.User

@Database(
    entities = [User::class, Category::class, ExpenseEntry::class, BudgetGoal::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseEntryDao(): ExpenseEntryDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codecash_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
