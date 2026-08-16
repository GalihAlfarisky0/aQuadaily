package com.aquadaily.app.core.repository

import com.aquadaily.app.core.database.dao.ReminderDao
import com.aquadaily.app.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(userId: Int): Flow<List<ReminderEntity>> =
        reminderDao.getAllReminders(userId)

    suspend fun getAllRemindersSync(userId: Int): List<ReminderEntity> =
        reminderDao.getAllRemindersSync(userId)

    suspend fun getReminderByIdSync(id: Int, userId: Int): ReminderEntity? =
        reminderDao.getReminderByIdSync(id, userId)

    suspend fun insertReminder(reminder: ReminderEntity): Long =
        reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) =
        reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderEntity) =
        reminderDao.deleteReminder(reminder)

    suspend fun updateReminderStatus(id: Int, userId: Int, isEnabled: Boolean) =
        reminderDao.updateReminderStatus(id, userId, isEnabled)

    suspend fun clearReminders(userId: Int) =
        reminderDao.clearReminders(userId)
}
