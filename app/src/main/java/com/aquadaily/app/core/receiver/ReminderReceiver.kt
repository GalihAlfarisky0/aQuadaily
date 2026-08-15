package com.aquadaily.app.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aquadaily.app.App
import com.aquadaily.app.core.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val appContext = context.applicationContext

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = appContext as App

            CoroutineScope(Dispatchers.IO).launch {
                val reminders =
                    app.reminderRepository.getAllRemindersSync()

                reminders
                    .filter { it.isEnabled }
                    .forEach { reminder ->
                        app.alarmScheduler.schedule(reminder)
                    }
            }

            return
        }

        val amount = intent.getIntExtra(
            "EXTRA_AMOUNT",
            250
        )

        val reminderId = intent.getIntExtra(
            "EXTRA_ID",
            -1
        )

        val notificationHelper =
            NotificationHelper(appContext)

        notificationHelper.showNotification(
            title = "Waktunya Minum Air 💧",
            message =
                "Jangan lupa minum $amount ml air sekarang untuk menjaga hidrasi kamu.",
            notificationId =
                if (reminderId != -1) reminderId else 1
        )

        // Schedule the same reminder for the next day only if it is still enabled.
        if (reminderId != -1) {
            val app = appContext as App

            CoroutineScope(Dispatchers.IO).launch {
                val reminder =
                    app.reminderRepository
                        .getReminderByIdSync(reminderId)

                if (reminder?.isEnabled == true) {
                    app.alarmScheduler.schedule(reminder)
                }
            }
        }
    }
}