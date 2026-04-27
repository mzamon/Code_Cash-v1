package com.codecash.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.codecash.app.data.entity.BudgetGoal

@Dao
interface BudgetGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BudgetGoal): Long

    @Update
    suspend fun updateGoal(goal: BudgetGoal)

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalForMonth(userId: Long, month: Int, year: Int): BudgetGoal?

    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getLatestGoal(userId: Long): BudgetGoal?

    @Query("DELETE FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun deleteGoalForMonth(userId: Long, month: Int, year: Int)
}
