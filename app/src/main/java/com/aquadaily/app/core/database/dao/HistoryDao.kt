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
        WHERE userId = :userId
        ORDER BY date DESC, time DESC
    """)
    fun getAllHistory(userId: Int): LiveData<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Update
    suspend fun updateHistory(history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("""
        SELECT * FROM history
        WHERE userId = :userId
        AND date = :date
        ORDER BY time DESC
    """)
    fun getHistoryByDate(userId: Int, date: String): LiveData<List<HistoryEntity>>

    @Query("""
        SELECT * FROM history
        WHERE userId = :userId
        AND (
            date LIKE '%' || :query || '%'
            OR time LIKE '%' || :query || '%'
            OR note LIKE '%' || :query || '%'
            OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
        )
        ORDER BY date DESC, time DESC
    """)
    fun searchHistory(userId: Int, query: String): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE id = :id AND userId = :userId")
    suspend fun getHistoryById(id: Int, userId: Int): HistoryEntity?

    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun clearHistory(userId: Int)

    @Query("""
        SELECT date, SUM(amount) AS totalAmount
        FROM history
        WHERE userId = :userId
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyWater(userId: Int): LiveData<List<DailyWater>>

    @Query("SELECT SUM(amount) FROM history WHERE userId = :userId AND date = :date")
    fun getTotalWaterByDate(userId: Int, date: String): LiveData<Int?>

    @Query("""
        SELECT date, SUM(amount) AS totalAmount
        FROM history
        WHERE userId = :userId
        AND date >= :startDate
        AND date <= :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getWeeklyWater(userId: Int, startDate: String, endDate: String): LiveData<List<DailyWater>>

    @Query("""
        SELECT substr(date, 1, 7) AS month, SUM(amount) AS totalAmount
        FROM history
        WHERE userId = :userId
        GROUP BY substr(date, 1, 7)
        ORDER BY month ASC
    """)
    fun getMonthlyWater(userId: Int): LiveData<List<MonthlyWater>>
}
