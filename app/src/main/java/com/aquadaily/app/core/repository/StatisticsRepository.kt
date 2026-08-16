package com.aquadaily.app.core.repository

import androidx.lifecycle.LiveData
import com.aquadaily.app.core.database.dao.HistoryDao
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater

class StatisticsRepository(
    private val historyDao: HistoryDao,
    private val userId: Int
) {

    val dailyWater: LiveData<List<DailyWater>> =
        historyDao.getDailyWater(userId)

    val monthlyWater: LiveData<List<MonthlyWater>> =
        historyDao.getMonthlyWater(userId)

    fun getWeeklyWater(
        startDate: String,
        endDate: String
    ): LiveData<List<DailyWater>> {
        return historyDao.getWeeklyWater(userId, startDate, endDate)
    }
}
