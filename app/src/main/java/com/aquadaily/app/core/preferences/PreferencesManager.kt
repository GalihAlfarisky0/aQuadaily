package com.aquadaily.app.core.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setCurrentUserId(userId: Int) {
        preferences.edit().putInt(KEY_CURRENT_USER_ID, userId).apply()
    }

    fun getCurrentUserId(): Int {
        return preferences.getInt(KEY_CURRENT_USER_ID, 0)
    }

    fun setUserName(name: String) {
        preferences.edit().putString(userKey(KEY_USER_NAME), name).apply()
    }

    fun getUserName(): String {
        return preferences.getString(userKey(KEY_USER_NAME), "User") ?: "User"
    }

    fun setEmail(email: String) {
        preferences.edit().putString(userKey(KEY_EMAIL), email).apply()
    }

    fun getEmail(): String {
        return preferences.getString(userKey(KEY_EMAIL), "") ?: ""
    }

    fun setWaterTarget(target: Int) {
        preferences.edit().putInt(userKey(KEY_WATER_TARGET), target).apply()
    }

    fun getWaterTarget(): Int {
        return preferences.getInt(userKey(KEY_WATER_TARGET), 2000)
    }

    fun setReminderInterval(interval: Int) {
        preferences.edit().putInt(userKey(KEY_INTERVAL), interval).apply()
    }

    fun getReminderInterval(): Int {
        return preferences.getInt(userKey(KEY_INTERVAL), 120)
    }

    fun setStartTime(time: String) {
        preferences.edit().putString(userKey(KEY_START_TIME), time).apply()
    }

    fun getStartTime(): String {
        return preferences.getString(userKey(KEY_START_TIME), "08:00") ?: "08:00"
    }

    fun setEndTime(time: String) {
        preferences.edit().putString(userKey(KEY_END_TIME), time).apply()
    }

    fun getEndTime(): String {
        return preferences.getString(userKey(KEY_END_TIME), "21:00") ?: "21:00"
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(userKey(KEY_NOTIFICATIONS_ENABLED), enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return preferences.getBoolean(userKey(KEY_NOTIFICATIONS_ENABLED), true)
    }

    fun setNotificationSoundEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(userKey(KEY_NOTIFICATION_SOUND), enabled).apply()
    }

    fun isNotificationSoundEnabled(): Boolean {
        return preferences.getBoolean(userKey(KEY_NOTIFICATION_SOUND), true)
    }

    fun setNotificationVibrateEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(userKey(KEY_NOTIFICATION_VIBRATE), enabled).apply()
    }

    fun isNotificationVibrateEnabled(): Boolean {
        return preferences.getBoolean(userKey(KEY_NOTIFICATION_VIBRATE), true)
    }

    fun setLoggedIn(value: Boolean) {
        preferences.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_LOGGED_IN, false) && getCurrentUserId() > 0
    }

    fun clearLogin() {
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_CURRENT_USER_ID)
            .apply()
    }

    fun clearSession() {
        clearLogin()
    }

    private fun userKey(base: String): String {
        val userId = getCurrentUserId()
        return "${base}_$userId"
    }

    companion object {
        private const val PREF_NAME = "aquadaily_preferences"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_WATER_TARGET = "water_target"
        private const val KEY_INTERVAL = "reminder_interval"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_NOTIFICATION_VIBRATE = "notification_vibrate"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
