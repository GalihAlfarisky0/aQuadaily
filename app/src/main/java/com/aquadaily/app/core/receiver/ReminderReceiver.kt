package com.aquadaily.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aquadaily.app.core.notification.NotificationHelper

import com.aquadaily.app.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as App
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = app.reminderRepository.getAllRemindersSync()
                reminders.filter { it.isEnabled }.forEach {
                    app.alarmScheduler.schedule(it)
                }
            }
            return
        }

        val amount = intent.getIntExtra("EXTRA_AMOUNT", 250)
        val reminderId = intent.getIntExtra("EXTRA_ID", -1)
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(
            "Time to Drink!",
            "Don't forget to drink $amount ml of water to stay hydrated."
        )

        // Reschedule for next day
        if (reminderId != -1) {
            val app = context.applicationContext as App
            CoroutineScope(Dispatchers.IO).launch {
                val reminder = app.reminderRepository.getReminderByIdSync(reminderId)
                reminder?.let {
                    if (it.isEnabled) {
                        app.alarmScheduler.schedule(it)
                    }
                }
            }
        }
    }
}