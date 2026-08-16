package com.aquadaily.app.ui.history

import androidx.lifecycle.*
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater
import com.aquadaily.app.core.repository.HistoryRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(
    private val repository: HistoryRepository,
    private val userId: Int
) : ViewModel() {

    private val _streak = MutableLiveData<Int>()
    val streak: LiveData<Int> = _streak

    private val _query = MutableLiveData("")

    val allHistory: LiveData<List<HistoryEntity>> = _query.switchMap { query ->
        if (query.isNullOrEmpty()) {
            repository.getAllHistory(userId)
        } else {
            repository.searchHistory(userId, query)
        }
    }

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

    fun search(query: String) {
        _query.value = query
    }

    fun getDailyWater(): LiveData<List<DailyWater>> =
        repository.getDailyWater(userId)

    fun getMonthlyWater(): LiveData<List<MonthlyWater>> =
        repository.getMonthlyWater(userId)

    fun getHistoryByDate(date: String): LiveData<List<HistoryEntity>> =
        repository.getHistoryByDate(userId, date)

    fun insert(history: HistoryEntity) {
        viewModelScope.launch {
            repository.insert(history.copy(userId = userId))
        }
    }

    fun update(history: HistoryEntity) {
        viewModelScope.launch {
            repository.update(history.copy(userId = userId))
        }
    }

    fun delete(history: HistoryEntity) {
        viewModelScope.launch {
            repository.delete(history.copy(userId = userId))
        }
    }

    suspend fun getHistoryById(id: Int): HistoryEntity? =
        repository.getHistoryById(id, userId)
}
