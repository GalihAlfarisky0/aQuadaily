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
    private val repository: HistoryRepository
) : ViewModel() {

    private val _streak = MutableLiveData<Int>()
    val streak: LiveData<Int> = _streak

    fun calculateStreak(dailyWaterList: List<DailyWater>, target: Int) {
        if (dailyWaterList.isEmpty()) {
            _streak.value = 0
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val waterMap = dailyWaterList.associateBy { it.date }
        
        var currentStreak = 0
        val checkDate = Calendar.getInstance()
        
        // If today hasn't reached target, streak might still be active from yesterday
        val todayStr = sdf.format(checkDate.time)
        val todayWater = waterMap[todayStr]?.totalAmount ?: 0
        if (todayWater < target) {
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

    private val _query = MutableLiveData<String>("")
    
    val allHistory: LiveData<List<HistoryEntity>> = _query.switchMap { q ->
        if (q.isNullOrEmpty()) {
            repository.allHistory
        } else {
            repository.searchHistory(q)
        }
    }

    fun search(query: String) {
        _query.value = query
    }

    fun getDailyWater(): LiveData<List<DailyWater>> {
        return repository.getDailyWater()
    }

    fun getMonthlyWater(): LiveData<List<MonthlyWater>> {
        return repository.getMonthlyWater()
    }

    fun getHistoryByDate(
        date: String
    ): LiveData<List<HistoryEntity>> {
        return repository.getHistoryByDate(date)
    }

    fun insert(
        history: HistoryEntity
    ) {
        viewModelScope.launch {
            repository.insert(history)
        }
    }

    fun update(
        history: HistoryEntity
    ) {
        viewModelScope.launch {
            repository.update(history)
        }
    }

    fun delete(
        history: HistoryEntity
    ) {
        viewModelScope.launch {
            repository.delete(history)
        }
    }

    suspend fun getHistoryById(id: Int): HistoryEntity? {
        return repository.getHistoryById(id)
    }
}