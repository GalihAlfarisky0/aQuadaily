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
 * AquaDaily reminder notification manager.
 *
 * Behaviour:
 * - Notification stays visible until water intake is recorded.
 * - User can choose Default System, AquaDaily Reminder, or Brand New Day.
 * - Sound and vibration can be toggled independently.
 * - Android 8+ uses dedicated channels so each sound/mode combination is stable.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_PREFIX = "aquadaily_reminder_v2"
        private val VIBRATION_PATTERN = longArrayOf(
            0L,
            350L,
            180L,
            450L,
            180L,
            650L
        )

        fun cancelActiveReminderNotifications(context: Context) {
            val preferences = PreferencesManager(context)
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            preferences.getActiveReminderNotificationIds().forEach { id ->
                manager.cancel(id)
            }

            preferences.clearActiveReminderNotificationIds()
        }
    }

    fun showNotification(
        title: String,
        message: String,
        notificationId: Int
    ) {
        val preferences = PreferencesManager(context)

        if (!preferences.isNotificationsEnabled()) return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannels()

        val channelId = resolveChannelId(preferences)

        val intent = Intent(
            context,
            DashboardActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra(
                "EXTRA_NOTIFICATION_ID",
                notificationId
            )
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
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(false)
            .setContentIntent(pendingIntent)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        manager.notify(
            notificationId,
            notification
        )

        preferences.addActiveReminderNotificationId(
            notificationId
        )
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val soundAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(
                AudioAttributes.CONTENT_TYPE_SONIFICATION
            )
            .build()

        val configurations = listOf(
            ChannelConfig(
                key = PreferencesManager.RINGTONE_DEFAULT,
                label = "Default System",
                sound = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                customAvailable = true
            ),
            ChannelConfig(
                key = PreferencesManager.RINGTONE_AQUADAILY,
                label = "AquaDaily Reminder",
                sound = resolveRawSound("aquadaily_reminder")
                    ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                customAvailable = resolveRawSound("aquadaily_reminder") != null
            ),
            ChannelConfig(
                key = PreferencesManager.RINGTONE_BRAND_NEW_DAY,
                label = "Brand New Day",
                sound = resolveRawSound("brand_new_day")
                    ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                customAvailable = resolveRawSound("brand_new_day") != null
            )
        )

        val modes = listOf(
            ModeConfig("sound_vibrate", "Sound + Vibration", true, true),
            ModeConfig("sound", "Sound", true, false),
            ModeConfig("vibrate", "Vibration", false, true),
            ModeConfig("silent", "Silent", false, false)
        )

        configurations.forEach { config ->
            modes.forEach { mode ->
                val stableAudioKey =
                    if (config.customAvailable) "custom" else "system"

                val channelId = channelId(
                    config.key,
                    mode.key,
                    stableAudioKey
                )

                val channel = NotificationChannel(
                    channelId,
                    "AquaDaily • ${config.label} • ${mode.label}",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description =
                        "Pengingat minum air AquaDaily"

                    lockscreenVisibility =
                        NotificationCompat.VISIBILITY_PUBLIC

                    enableVibration(mode.vibrate)

                    if (mode.vibrate) {
                        vibrationPattern =
                            VIBRATION_PATTERN
                    }

                    if (mode.sound) {
                        setSound(
                            config.sound,
                            soundAttributes
                        )
                    } else {
                        setSound(null, null)
                    }
                }

                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun resolveChannelId(
        preferences: PreferencesManager
    ): String {
        val ringtoneKey =
            preferences.getNotificationRingtone()

        val modeKey = when {
            preferences.isNotificationSoundEnabled() &&
                preferences.isNotificationVibrateEnabled() ->
                "sound_vibrate"

            preferences.isNotificationSoundEnabled() ->
                "sound"

            preferences.isNotificationVibrateEnabled() ->
                "vibrate"

            else ->
                "silent"
        }

        val customAvailable = when (ringtoneKey) {
            PreferencesManager.RINGTONE_AQUADAILY ->
                resolveRawSound("aquadaily_reminder") != null

            PreferencesManager.RINGTONE_BRAND_NEW_DAY ->
                resolveRawSound("brand_new_day") != null

            else -> true
        }

        return channelId(
            ringtoneKey,
            modeKey,
            if (customAvailable) "custom" else "system"
        )
    }

    private fun channelId(
        ringtoneKey: String,
        modeKey: String,
        audioKey: String
    ): String =
        "${CHANNEL_PREFIX}_${ringtoneKey}_${modeKey}_$audioKey"

    private fun resolveRawSound(
        rawName: String
    ): Uri? {
        val resourceId = context.resources.getIdentifier(
            rawName,
            "raw",
            context.packageName
        )

        if (resourceId == 0) return null

        return Uri.parse(
            "android.resource://${context.packageName}/$resourceId"
        )
    }

    private data class ChannelConfig(
        val key: String,
        val label: String,
        val sound: Uri,
        val customAvailable: Boolean
    )

    private data class ModeConfig(
        val key: String,
        val label: String,
        val sound: Boolean,
        val vibrate: Boolean
    )
}