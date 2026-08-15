package com.aquadaily.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aquadaily.app.MainActivity
import com.aquadaily.app.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "reminder_channel"
        private const val CHANNEL_NAME = "Water Reminder"
    }

    fun showNotification(title: String, message: String) {
        val preferences = com.aquadaily.app.core.preferences.PreferencesManager(context)
        if (!preferences.isNotificationsEnabled()) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.aquadaily.app.ui.dashboard.DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ganti dengan icon yang sesuai
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .apply {
                if (!preferences.isNotificationSoundEnabled()) {
                    setSound(null)
                }
                if (!preferences.isNotificationVibrateEnabled()) {
                    setVibrate(longArrayOf(0L))
                }
            }
            .build()

        notificationManager.notify(1, notification)
    }
}