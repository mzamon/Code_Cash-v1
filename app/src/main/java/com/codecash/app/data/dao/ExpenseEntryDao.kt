package com.codecash.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.codecash.app.data.entity.ExpenseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ExpenseEntry): Long

    @Update
    suspend fun updateEntry(entry: ExpenseEntry)

    @Delete
    suspend fun deleteEntry(entry: ExpenseEntry)

    @Query("SELECT * FROM expense_entries WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllEntries(userId: Long): Flow<List<ExpenseEntry>>

    @Query("""
        SELECT * FROM expense_entries
        WHERE userId = :userId
          AND date >= :startDate
          AND date <= :endDate
        ORDER BY date DESC, startTime DESC
    """)
    suspend fun getEntriesInPeriod(userId: Long, startDate: String, endDate: String): List<ExpenseEntry>

    @Query("""
        SELECT categoryId, SUM(amount) as total
        FROM expense_entries
        WHERE userId = :userId
          AND date >= :startDate
          AND date <= :endDate
        GROUP BY categoryId
    """)
    suspend fun getCategoryTotalsInPeriod(
        userId: Long,
        startDate: String,
        endDate: String
    ): List<CategoryTotal>

    @Query("""
        SELECT SUM(amount) FROM expense_entries
        WHERE userId = :userId
          AND date LIKE :monthPattern
    """)
    suspend fun getTotalSpentInMonth(userId: Long, monthPattern: String): Double?

    @Query("SELECT * FROM expense_entries WHERE userId = :userId ORDER BY date DESC, startTime DESC LIMIT 5")
    fun getRecentEntries(userId: Long): Flow<List<ExpenseEntry>>

    @Query("SELECT * FROM expense_entries WHERE id = :entryId LIMIT 1")
    suspend fun getEntryById(entryId: Long): ExpenseEntry?
}

data class CategoryTotal(
    val categoryId: Long?,
    val total: Double
)
