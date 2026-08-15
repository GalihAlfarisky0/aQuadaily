package com.aquadaily.app.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aquadaily.app.core.database.entity.UserEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    val user = userRepository.getUser().asLiveData()
    val dailyWater = historyRepository.getDailyWater()

    private val _streak = MutableLiveData<Int>()
    val streak: androidx.lifecycle.LiveData<Int> = _streak

    fun calculateStreak(dailyWaterList: List<DailyWater>, target: Int) {
        if (dailyWaterList.isEmpty()) {
            _streak.value = 0
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // Today
        val todayStr = sdf.format(calendar.time)
        
        // Yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)

        val waterMap = dailyWaterList.associateBy { it.date }
        
        var currentStreak = 0
        var checkDate = Calendar.getInstance()
        
        // Start checking from today
        var dateStr = sdf.format(checkDate.time)
        
        // If today hasn't reached target, streak might still be active from yesterday
        val todayWater = waterMap[dateStr]?.totalAmount ?: 0
        if (todayWater < target) {
            // Check from yesterday backwards
            checkDate.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dStr = sdf.format(checkDate.time)
            val amount = waterMap[dStr]?.totalAmount ?: 0
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
            userRepository.updateUser(user)
        }
    }

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            userRepository.insertUser(user)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            userRepository.deleteUser()
        }
    }
}
