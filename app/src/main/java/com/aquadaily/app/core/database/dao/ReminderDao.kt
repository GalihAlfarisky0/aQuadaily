package com.aquadaily.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aquadaily.app.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder")
    suspend fun getAllRemindersSync(): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE id = :id")
    fun getReminderById(id: Int): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getReminderByIdSync(id: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("UPDATE reminder SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, isEnabled: Boolean)
}