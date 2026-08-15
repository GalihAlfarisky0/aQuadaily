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
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val todayWaterIntake: LiveData<Int?> = historyRepository.getTotalWaterByDate(getCurrentDate())
    val todayHistory: LiveData<List<HistoryEntity>> = historyRepository.getHistoryByDate(getCurrentDate())
    val reminders = reminderRepository.getAllReminders().asLiveData()

    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    private fun getCurrentTime(): String {
        return timeFormat.format(Date())
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            val history = HistoryEntity(
                amount = amount,
                date = getCurrentDate(),
                time = getCurrentTime(),
                note = "Quick Add"
            )
            historyRepository.insert(history)
        }
    }
}
