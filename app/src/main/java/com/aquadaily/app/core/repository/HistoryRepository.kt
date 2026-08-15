package com.aquadaily.app.core.repository

import androidx.lifecycle.LiveData
import com.aquadaily.app.core.database.dao.HistoryDao
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater

class HistoryRepository(
    private val historyDao: HistoryDao
) {

    val allHistory: LiveData<List<HistoryEntity>> =
        historyDao.getAllHistory()

    fun getHistoryByDate(
        date: String
    ): LiveData<List<HistoryEntity>> {

        return historyDao.getHistoryByDate(date)
    }

    fun searchHistory(
        query: String
    ): LiveData<List<HistoryEntity>> {

        return historyDao.searchHistory(query)
    }

    suspend fun insert(
        history: HistoryEntity
    ) {
        historyDao.insertHistory(history)
    }

    suspend fun update(
        history: HistoryEntity
    ) {
        historyDao.updateHistory(history)
    }

    suspend fun delete(
        history: HistoryEntity
    ) {
        historyDao.deleteHistory(history)
    }

    suspend fun getHistoryById(id: Int): HistoryEntity? {
        return historyDao.getHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    fun getTotalWaterByDate(date: String): LiveData<Int?> {
        return historyDao.getTotalWaterByDate(date)
    }

    fun getDailyWater(): LiveData<List<DailyWater>> {
        return historyDao.getDailyWater()
    }

    fun getMonthlyWater(): LiveData<List<MonthlyWater>> {
        return historyDao.getMonthlyWater()
    }
}