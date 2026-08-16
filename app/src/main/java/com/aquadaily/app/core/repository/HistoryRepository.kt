package com.aquadaily.app.core.repository

import androidx.lifecycle.LiveData
import com.aquadaily.app.core.database.dao.HistoryDao
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater

class HistoryRepository(
    private val historyDao: HistoryDao
) {

    fun getAllHistory(userId: Int): LiveData<List<HistoryEntity>> =
        historyDao.getAllHistory(userId)

    fun getHistoryByDate(userId: Int, date: String): LiveData<List<HistoryEntity>> =
        historyDao.getHistoryByDate(userId, date)

    fun searchHistory(userId: Int, query: String): LiveData<List<HistoryEntity>> =
        historyDao.searchHistory(userId, query)

    suspend fun insert(history: HistoryEntity) =
        historyDao.insertHistory(history)

    suspend fun update(history: HistoryEntity) =
        historyDao.updateHistory(history)

    suspend fun delete(history: HistoryEntity) =
        historyDao.deleteHistory(history)

    suspend fun getHistoryById(id: Int, userId: Int): HistoryEntity? =
        historyDao.getHistoryById(id, userId)

    suspend fun clearHistory(userId: Int) =
        historyDao.clearHistory(userId)

    fun getTotalWaterByDate(userId: Int, date: String): LiveData<Int?> =
        historyDao.getTotalWaterByDate(userId, date)

    fun getDailyWater(userId: Int): LiveData<List<DailyWater>> =
        historyDao.getDailyWater(userId)

    fun getMonthlyWater(userId: Int): LiveData<List<MonthlyWater>> =
        historyDao.getMonthlyWater(userId)
}
