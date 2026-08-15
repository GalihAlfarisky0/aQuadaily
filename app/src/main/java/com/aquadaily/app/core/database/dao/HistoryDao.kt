package com.aquadaily.app.core.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater

@Dao
interface HistoryDao {

    @Query("""
        SELECT * FROM history
        ORDER BY date DESC, time DESC
    """)
    fun getAllHistory(): LiveData<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Update
    suspend fun updateHistory(history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("""
        SELECT * FROM history
        WHERE date = :date
        ORDER BY time DESC
    """)
    fun getHistoryByDate(
        date: String
    ): LiveData<List<HistoryEntity>>

    @Query("""
        SELECT * FROM history
        WHERE date LIKE '%' || :query || '%'
        OR time LIKE '%' || :query || '%'
        OR note LIKE '%' || :query || '%'
        OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
        ORDER BY date DESC, time DESC
    """)
    fun searchHistory(
        query: String
    ): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: Int): HistoryEntity?

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Query("""
        SELECT date, SUM(amount) AS totalAmount
        FROM history
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyWater(): LiveData<List<DailyWater>>

    @Query("SELECT SUM(amount) FROM history WHERE date = :date")
    fun getTotalWaterByDate(date: String): LiveData<Int?>

    @Query("""
        SELECT date, SUM(amount) AS totalAmount
        FROM history
        WHERE date >= :startDate
        AND date <= :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getWeeklyWater(
        startDate: String,
        endDate: String
    ): LiveData<List<DailyWater>>

    @Query("""
        SELECT 
            substr(date, 1, 7) AS month,
            SUM(amount) AS totalAmount
        FROM history
        GROUP BY substr(date, 1, 7)
        ORDER BY month ASC
    """)
    fun getMonthlyWater(): LiveData<List<MonthlyWater>>
}