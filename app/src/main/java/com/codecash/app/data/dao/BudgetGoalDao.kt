package com.codecash.app.data.dao

import androidx.room.*
import com.codecash.app.data.entity.BudgetGoalEntity

@Dao
interface BudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: BudgetGoalEntity)

    @Query("SELECT * FROM budget_goals WHERE monthYear = :monthYear LIMIT 1")
    suspend fun getGoalForMonth(monthYear: String): BudgetGoalEntity?
}
