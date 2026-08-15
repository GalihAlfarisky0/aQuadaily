package com.aquadaily.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aquadaily.app.core.database.entity.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLog>>

    @Insert
    suspend fun insertLog(log: WaterLog)

    @Query("SELECT SUM(amount) FROM water_logs WHERE timestamp >= :startOfDay")
    fun getTotalAmountToday(startOfDay: Long): Flow<Int?>
}