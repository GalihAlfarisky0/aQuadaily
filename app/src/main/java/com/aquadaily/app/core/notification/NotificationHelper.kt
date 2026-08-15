package com.aquadaily.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aquadaily.app.R
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.ui.dashboard.DashboardActivity

/**
 * Centralized AquaDaily notification manager.
 *
 * Supports:
 * - High-priority reminder notifications
 * - Vibration patterns
 * - Custom ringtone from res/raw/aquadaily_reminder.mp3
 * - Separate channels for sound/vibration combinations
 * - Android 13+ notification permission awareness
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_SOUND_VIBRATE = "water_reminder_sound_vibrate"
        private const val CHANNEL_SOUND_ONLY = "water_reminder_sound_only"
        private const val CHANNEL_VIBRATE_ONLY = "water_reminder_vibrate_only"

        private const val CHANNEL_NAME_SOUND_VIBRATE = "Water Reminder • Sound & Vibration"
        private const val CHANNEL_NAME_SOUND_ONLY = "Water Reminder • Sound"
        private const val CHANNEL_NAME_VIBRATE_ONLY = "Water Reminder • Vibration"

        private val VIBRATION_PATTERN = longArrayOf(
            0L,
            350L,
            180L,
            450L,
            180L,
            650L
        )
    }

    fun showNotification(
        title: String,
        message: String,
        notificationId: Int = 1
    ) {
        val preferences = PreferencesManager(context)

        if (!preferences.isNotificationsEnabled()) {
            return
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannels()

        val channelId = when {
            preferences.isNotificationSoundEnabled() &&
                preferences.isNotificationVibrateEnabled() -> CHANNEL_SOUND_VIBRATE

            preferences.isNotificationSoundEnabled() -> CHANNEL_SOUND_ONLY

            preferences.isNotificationVibrateEnabled() -> CHANNEL_VIBRATE_ONLY

            else -> CHANNEL_VIBRATE_ONLY
        }

        val intent = Intent(
            context,
            DashboardActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra("EXTRA_NOTIFICATION_ID", notificationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            notificationId,
            notification
        )
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val customSound = Uri.parse(
            "android.resource://${context.packageName}/${R.raw.aquadaily_reminder}"
        )

        val soundAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val soundAndVibrate = NotificationChannel(
            CHANNEL_SOUND_VIBRATE,
            CHANNEL_NAME_SOUND_VIBRATE,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "AquaDaily reminder dengan suara dan getaran"
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            setSound(customSound, soundAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val soundOnly = NotificationChannel(
            CHANNEL_SOUND_ONLY,
            CHANNEL_NAME_SOUND_ONLY,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "AquaDaily reminder dengan suara"
            enableVibration(false)
            setSound(customSound, soundAttributes)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        val vibrateOnly = NotificationChannel(
            CHANNEL_VIBRATE_ONLY,
            CHANNEL_NAME_VIBRATE_ONLY,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "AquaDaily reminder dengan getaran"
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            setSound(null, null)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(soundAndVibrate)
        manager.createNotificationChannel(soundOnly)
        manager.createNotificationChannel(vibrateOnly)
    }
}