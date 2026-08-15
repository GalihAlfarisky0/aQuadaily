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
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val allReminders = repository.getAllReminders().asLiveData()

    fun insertReminder(reminder: ReminderEntity) = viewModelScope.launch {
        val id = repository.insertReminder(reminder)
        if (reminder.isEnabled) {
            alarmScheduler.schedule(reminder.copy(id = id.toInt()))
        }
    }

    fun updateReminder(reminder: ReminderEntity) = viewModelScope.launch {
        repository.updateReminder(reminder)
        if (reminder.isEnabled) {
            alarmScheduler.schedule(reminder)
        } else {
            alarmScheduler.cancel(reminder)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch {
        repository.deleteReminder(reminder)
        alarmScheduler.cancel(reminder)
    }

    fun updateReminderStatus(reminder: ReminderEntity, isEnabled: Boolean) = viewModelScope.launch {
        val updatedReminder = reminder.copy(isEnabled = isEnabled)
        repository.updateReminder(updatedReminder)
        if (isEnabled) {
            alarmScheduler.schedule(updatedReminder)
        } else {
            alarmScheduler.cancel(updatedReminder)
        }
    }

    class Factory(
        private val repository: ReminderRepository,
        private val alarmScheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReminderViewModel(repository, alarmScheduler) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}