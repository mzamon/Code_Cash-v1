package com.codecash.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals")
data class BudgetGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monthYear: String,
    val minGoal: Double,
    val maxGoal: Double
)
