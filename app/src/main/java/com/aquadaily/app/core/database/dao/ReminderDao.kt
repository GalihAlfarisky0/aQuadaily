package com.aquadaily.app.core.database.dao

import androidx.room.*
import com.aquadaily.app.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminder WHERE userId = :userId ORDER BY hour ASC, minute ASC")
    fun getAllReminders(userId: Int): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder WHERE userId = :userId")
    suspend fun getAllRemindersSync(userId: Int): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE id = :id AND userId = :userId")
    fun getReminderById(id: Int, userId: Int): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminder WHERE id = :id AND userId = :userId")
    suspend fun getReminderByIdSync(id: Int, userId: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminder SET isEnabled = :isEnabled WHERE id = :id AND userId = :userId")
    suspend fun updateReminderStatus(id: Int, userId: Int, isEnabled: Boolean)

    @Query("DELETE FROM reminder WHERE userId = :userId")
    suspend fun clearReminders(userId: Int)
}
