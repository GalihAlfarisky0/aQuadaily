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
 * Sounds stored in res/raw are addressed with android.resource:// URIs so
 * they work independently from the device's system ringtone library.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_PREFIX = "water_reminder"
        private const val CHANNEL_NAME = "Water Reminder"

        private val VIBRATION_PATTERN = longArrayOf(
            0L,
            350L,
            180L,
            450L,
            180L,
            650L
        )
    }

    /**
     * Initializes the notification channels used by AquaDaily.
     * Kept as a public API because App.kt calls this once during startup.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val preferences = PreferencesManager(context)
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the currently selected channel so the app is ready before
        // the first reminder fires. Bundled sounds are resolved through R.raw.
        val selectedSoundUri = getSelectedSoundUri(preferences)
        val selectedChannelId = getChannelId(preferences, selectedSoundUri)
        createNotificationChannel(selectedChannelId, preferences, selectedSoundUri)

        // Also register each bundled sound as its own channel. Android 8+
        // requires a channel's sound to be fixed at creation time, so separate
        // channels let AquaDaily switch sounds reliably later.
        NotificationSoundCatalog.getBundledSounds().forEach { sound ->
            val uri = NotificationSoundCatalog.toUri(context, sound)
            val channelId = "${CHANNEL_PREFIX}_bundled_${sound.id}"
            val channelPreferencesName = sound.name

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "$CHANNEL_NAME • $channelPreferencesName",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "AquaDaily reminder"
                    enableVibration(preferences.isNotificationVibrateEnabled())
                    if (preferences.isNotificationVibrateEnabled()) {
                        vibrationPattern = VIBRATION_PATTERN
                    }
                    if (preferences.isNotificationSoundEnabled()) {
                        setSound(uri, attributes)
                    } else {
                        setSound(null, null)
                    }
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
            }
        }
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

        val soundUri = getSelectedSoundUri(preferences)
        val channelId = getChannelId(preferences, soundUri)
        createNotificationChannel(channelId, preferences, soundUri)

        val intent = Intent(context, DashboardActivity::class.java).apply {
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
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(
        channelId: String,
        preferences: PreferencesManager,
        soundUri: Uri?
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel = NotificationChannel(
            channelId,
            "$CHANNEL_NAME • ${preferences.getNotificationSoundName()}",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "AquaDaily reminder"
            enableVibration(preferences.isNotificationVibrateEnabled())

            if (preferences.isNotificationVibrateEnabled()) {
                vibrationPattern = VIBRATION_PATTERN
            }

            if (preferences.isNotificationSoundEnabled()) {
                setSound(
                    soundUri ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    soundAttributes
                )
            } else {
                setSound(null, null)
            }

            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }

        manager.createNotificationChannel(channel)
    }

    private fun getSelectedSoundUri(preferences: PreferencesManager): Uri? {
        val storedUri = preferences.getNotificationSoundUri()
        if (storedUri.isNullOrBlank()) {
            return android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        }

        val selectedId = storedUri.removePrefix("aquadaily://sound/")
        val bundledSound = NotificationSoundCatalog.findById(selectedId)
        if (bundledSound != null) {
            return NotificationSoundCatalog.toUri(context, bundledSound)
        }

        return try {
            Uri.parse(storedUri)
        } catch (_: Exception) {
            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
        }
    }

    private fun getChannelId(
        preferences: PreferencesManager,
        soundUri: Uri?
    ): String {
        val soundKey = soundUri?.toString()?.hashCode()?.toUInt()?.toString(16) ?: "default"
        val soundState = if (preferences.isNotificationSoundEnabled()) "sound" else "silent"
        val vibrationState = if (preferences.isNotificationVibrateEnabled()) "vibrate" else "novibrate"
        return "${CHANNEL_PREFIX}_${soundState}_${vibrationState}_$soundKey"
    }
}
