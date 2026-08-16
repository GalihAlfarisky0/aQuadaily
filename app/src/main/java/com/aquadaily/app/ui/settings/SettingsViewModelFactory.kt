package com.aquadaily.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository
import com.aquadaily.app.core.repository.UserRepository

class SettingsViewModelFactory(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository,
    private val reminderRepository: ReminderRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                userRepository,
                historyRepository,
                reminderRepository,
                userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
