package com.aquadaily.app.core.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setUserName(name: String) {
        preferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String =
        preferences.getString(KEY_USER_NAME, "User") ?: "User"

    fun setEmail(email: String) {
        preferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String =
        preferences.getString(KEY_EMAIL, "") ?: ""

    fun setUserId(userId: Int) {
        preferences.edit().putInt(KEY_CURRENT_USER_ID, userId).apply()
    }

    fun getCurrentUserId(): Int =
        preferences.getInt(KEY_CURRENT_USER_ID, -1)

    fun setCurrentUserId(userId: Int) {
        setUserId(userId)
    }

    fun setWaterTarget(target: Int) {
        preferences.edit().putInt(KEY_WATER_TARGET, target).apply()
    }

    fun getWaterTarget(): Int =
        preferences.getInt(KEY_WATER_TARGET, 2000)

    fun setReminderInterval(interval: Int) {
        preferences.edit().putInt(KEY_INTERVAL, interval).apply()
    }

    fun getReminderInterval(): Int =
        preferences.getInt(KEY_INTERVAL, 120)

    fun setStartTime(time: String) {
        preferences.edit().putString(KEY_START_TIME, time).apply()
    }

    fun getStartTime(): String =
        preferences.getString(KEY_START_TIME, "08:00") ?: "08:00"

    fun setEndTime(time: String) {
        preferences.edit().putString(KEY_END_TIME, time).apply()
    }

    fun getEndTime(): String =
        preferences.getString(KEY_END_TIME, "21:00") ?: "21:00"

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean =
        preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    fun setNotificationSoundEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_SOUND, enabled).apply()
    }

    fun isNotificationSoundEnabled(): Boolean =
        preferences.getBoolean(KEY_NOTIFICATION_SOUND, true)

    fun setNotificationVibrateEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_VIBRATE, enabled).apply()
    }

    fun isNotificationVibrateEnabled(): Boolean =
        preferences.getBoolean(KEY_NOTIFICATION_VIBRATE, true)

    fun setNotificationRingtone(ringtone: String) {
        preferences.edit().putString(KEY_NOTIFICATION_RINGTONE, ringtone).apply()
    }

    fun getNotificationRingtone(): String =
        preferences.getString(KEY_NOTIFICATION_RINGTONE, RINGTONE_DEFAULT)
            ?: RINGTONE_DEFAULT

    fun addActiveReminderNotificationId(notificationId: Int) {
        val current = getActiveReminderNotificationIds().toMutableSet()
        current.add(notificationId)
        preferences.edit()
            .putStringSet(KEY_ACTIVE_NOTIFICATION_IDS, current.map(Int::toString).toSet())
            .apply()
    }

    fun getActiveReminderNotificationIds(): Set<Int> {
        return preferences.getStringSet(KEY_ACTIVE_NOTIFICATION_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    fun clearActiveReminderNotificationIds() {
        preferences.edit().remove(KEY_ACTIVE_NOTIFICATION_IDS).apply()
    }

    fun setLoggedIn(value: Boolean) {
        preferences.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    fun isLoggedIn(): Boolean =
        preferences.getBoolean(KEY_LOGGED_IN, false)

    fun clearLogin() {
        preferences.edit()
            .remove(KEY_CURRENT_USER_ID)
            .putBoolean(KEY_LOGGED_IN, false)
            .apply()
    }

    companion object {
        const val RINGTONE_DEFAULT = "default"
        const val RINGTONE_AQUADAILY = "aquadaily"
        const val RINGTONE_BRAND_NEW_DAY = "brand_new_day"

        private const val PREF_NAME = "aquadaily_preferences"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_WATER_TARGET = "water_target"
        private const val KEY_INTERVAL = "reminder_interval"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_NOTIFICATION_VIBRATE = "notification_vibrate"
        private const val KEY_NOTIFICATION_RINGTONE = "notification_ringtone"
        private const val KEY_ACTIVE_NOTIFICATION_IDS = "active_reminder_notification_ids"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
