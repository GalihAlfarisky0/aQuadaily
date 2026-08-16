package com.aquadaily.app.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aquadaily.app.core.database.entity.UserEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository
import com.aquadaily.app.core.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository,
    private val reminderRepository: ReminderRepository,
    private val userId: Int
) : ViewModel() {

    val user = userRepository.getUserById(userId).asLiveData()
    val dailyWater = historyRepository.getDailyWater(userId)

    private val _streak = MutableLiveData<Int>()
    val streak: androidx.lifecycle.LiveData<Int> = _streak

    fun calculateStreak(dailyWaterList: List<DailyWater>, target: Int) {
        if (dailyWaterList.isEmpty()) {
            _streak.value = 0
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val waterMap = dailyWaterList.associateBy { it.date }
        var currentStreak = 0
        val checkDate = Calendar.getInstance()

        val todayStr = sdf.format(checkDate.time)
        val todayWater = waterMap[todayStr]?.totalAmount ?: 0
        if (todayWater < target) {
            checkDate.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dateStr = sdf.format(checkDate.time)
            val amount = waterMap[dateStr]?.totalAmount ?: 0
            if (amount >= target) {
                currentStreak++
                checkDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        _streak.value = currentStreak
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            userRepository.updateUser(user.copy(id = userId))
        }
    }

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            userRepository.insertUser(user)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            historyRepository.clearHistory(userId)
            reminderRepository.clearReminders(userId)
            userRepository.deleteUserById(userId)
        }
    }
}
