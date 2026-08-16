package com.aquadaily.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
 * - Android 8+ uses dedicated channels for the selected sound/mode.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_PREFIX = "aquadaily_reminder_v3"

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
            ) != PackageManager.PERMISSION_GRANTED
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

        manager.notify(notificationId, notification)
        preferences.addActiveReminderNotificationId(notificationId)
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val soundAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val ringtoneConfigs = listOf(
            RingtoneConfig(
                key = PreferencesManager.RINGTONE_DEFAULT,
                label = "Default System",
                sound = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            ),
            RingtoneConfig(
                key = PreferencesManager.RINGTONE_AQUADAILY,
                label = "AquaDaily Reminder",
                sound = resolveRawSound("aquadaily_reminder")
                    ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            ),
            RingtoneConfig(
                key = PreferencesManager.RINGTONE_BRAND_NEW_DAY,
                label = "Brand New Day",
                sound = resolveRawSound("brand_new_day")
                    ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            )
        )

        ringtoneConfigs.forEach { ringtone ->
            createChannel(
                manager = manager,
                ringtone = ringtone,
                modeKey = "sound_vibrate",
                modeLabel = "Sound + Vibration",
                soundEnabled = true,
                vibrationEnabled = true,
                soundAttributes = soundAttributes
            )

            createChannel(
                manager = manager,
                ringtone = ringtone,
                modeKey = "sound",
                modeLabel = "Sound",
                soundEnabled = true,
                vibrationEnabled = false,
                soundAttributes = soundAttributes
            )

            createChannel(
                manager = manager,
                ringtone = ringtone,
                modeKey = "vibrate",
                modeLabel = "Vibration",
                soundEnabled = false,
                vibrationEnabled = true,
                soundAttributes = soundAttributes
            )

            createChannel(
                manager = manager,
                ringtone = ringtone,
                modeKey = "silent",
                modeLabel = "Silent",
                soundEnabled = false,
                vibrationEnabled = false,
                soundAttributes = soundAttributes
            )
        }
    }

    private fun createChannel(
        manager: NotificationManager,
        ringtone: RingtoneConfig,
        modeKey: String,
        modeLabel: String,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        soundAttributes: AudioAttributes
    ) {
        val channelId =
            "${CHANNEL_PREFIX}_${ringtone.key}_${modeKey}"

        val channel = NotificationChannel(
            channelId,
            "AquaDaily • ${ringtone.label} • $modeLabel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat minum air AquaDaily"
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            enableVibration(vibrationEnabled)

            if (vibrationEnabled) {
                vibrationPattern = VIBRATION_PATTERN
            }

            if (soundEnabled) {
                setSound(ringtone.sound, soundAttributes)
            } else {
                setSound(null, null)
            }
        }

        manager.createNotificationChannel(channel)
    }

    private fun resolveChannelId(
        preferences: PreferencesManager
    ): String {
        val ringtoneKey = preferences.getNotificationRingtone()

        val modeKey = when {
            preferences.isNotificationSoundEnabled() &&
                preferences.isNotificationVibrateEnabled() -> {
                "sound_vibrate"
            }

            preferences.isNotificationSoundEnabled() -> {
                "sound"
            }

            preferences.isNotificationVibrateEnabled() -> {
                "vibrate"
            }

            else -> {
                "silent"
            }
        }

        return "${CHANNEL_PREFIX}_${ringtoneKey}_$modeKey"
    }

    private fun resolveRawSound(rawName: String): Uri? {
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

    private data class RingtoneConfig(
        val key: String,
        val label: String,
        val sound: Uri
    )
}
