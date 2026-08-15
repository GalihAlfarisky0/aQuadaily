package com.aquadaily.app.core.repository

import androidx.lifecycle.LiveData
import com.aquadaily.app.core.database.dao.HistoryDao
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater

class StatisticsRepository(
    private val historyDao: HistoryDao
) {

    val dailyWater: LiveData<List<DailyWater>> =
        historyDao.getDailyWater()

    val monthlyWater: LiveData<List<MonthlyWater>> =
        historyDao.getMonthlyWater()

    fun getWeeklyWater(
        startDate: String,
        endDate: String
    ): LiveData<List<DailyWater>> {
        return historyDao.getWeeklyWater(startDate, endDate)
    }
}