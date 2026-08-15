package com.aquadaily.app.core.repository

import com.aquadaily.app.core.database.dao.ReminderDao
import com.aquadaily.app.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun getAllRemindersSync(): List<ReminderEntity> = reminderDao.getAllRemindersSync()

    suspend fun getReminderByIdSync(id: Int): ReminderEntity? = reminderDao.getReminderByIdSync(id)

    suspend fun insertReminder(reminder: ReminderEntity): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)

    suspend fun updateReminderStatus(id: Int, isEnabled: Boolean) = 
        reminderDao.updateReminderStatus(id, isEnabled)
}