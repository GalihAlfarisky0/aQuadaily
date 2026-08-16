package com.aquadaily.app.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aquadaily.app.core.alarm.AlarmScheduler
import com.aquadaily.app.core.database.entity.ReminderEntity
import com.aquadaily.app.core.repository.ReminderRepository
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val userId: Int
) : ViewModel() {

    val allReminders = repository.getAllReminders(userId).asLiveData()

    fun insertReminder(reminder: ReminderEntity) = viewModelScope.launch {
        if (userId <= 0) return@launch
        val userReminder = reminder.copy(userId = userId)
        val id = repository.insertReminder(userReminder)
        if (userReminder.isEnabled) {
            alarmScheduler.schedule(userReminder.copy(id = id.toInt()))
        }
    }

    fun updateReminder(reminder: ReminderEntity) = viewModelScope.launch {
        val userReminder = reminder.copy(userId = userId)
        repository.updateReminder(userReminder)
        if (userReminder.isEnabled) {
            alarmScheduler.schedule(userReminder)
        } else {
            alarmScheduler.cancel(userReminder)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch {
        val userReminder = reminder.copy(userId = userId)
        repository.deleteReminder(userReminder)
        alarmScheduler.cancel(userReminder)
    }

    fun updateReminderStatus(reminder: ReminderEntity, isEnabled: Boolean) = viewModelScope.launch {
        val updatedReminder = reminder.copy(userId = userId, isEnabled = isEnabled)
        repository.updateReminder(updatedReminder)
        if (isEnabled) {
            alarmScheduler.schedule(updatedReminder)
        } else {
            alarmScheduler.cancel(updatedReminder)
        }
    }

    class Factory(
        private val repository: ReminderRepository,
        private val alarmScheduler: AlarmScheduler,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReminderViewModel(repository, alarmScheduler, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
