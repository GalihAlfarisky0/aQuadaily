package com.aquadaily.app

import android.app.Application
import com.aquadaily.app.core.alarm.AlarmScheduler
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.notification.NotificationHelper
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository

class App : Application() {

    lateinit var database: AppDatabase
    lateinit var reminderRepository: ReminderRepository
    lateinit var historyRepository: HistoryRepository
    lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)

        reminderRepository = ReminderRepository(database.reminderDao())
        historyRepository = HistoryRepository(database.historyDao())
        alarmScheduler = AlarmScheduler(this)

        // Create AquaDaily notification channels once at app startup.
        NotificationHelper(this).createNotificationChannels()
    }
}