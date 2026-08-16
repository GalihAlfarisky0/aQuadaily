package com.aquadaily.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository

class DashboardViewModelFactory(
    private val historyRepository: HistoryRepository,
    private val reminderRepository: ReminderRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                historyRepository,
                reminderRepository,
                userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
