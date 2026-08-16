package com.aquadaily.app.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(
    private val historyRepository: HistoryRepository,
    private val reminderRepository: ReminderRepository,
    private val userId: Int
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val todayWaterIntake: LiveData<Int?> =
        historyRepository.getTotalWaterByDate(userId, getCurrentDate())

    val todayHistory: LiveData<List<HistoryEntity>> =
        historyRepository.getHistoryByDate(userId, getCurrentDate())

    val reminders = reminderRepository
        .getAllReminders(userId)
        .asLiveData()

    private fun getCurrentDate(): String = dateFormat.format(Date())

    private fun getCurrentTime(): String = timeFormat.format(Date())

    fun addWater(amount: Int) {
        if (userId <= 0) return

        viewModelScope.launch {
            val history = HistoryEntity(
                userId = userId,
                amount = amount,
                date = getCurrentDate(),
                time = getCurrentTime(),
                note = "Quick Add"
            )
            historyRepository.insert(history)
        }
    }
}
