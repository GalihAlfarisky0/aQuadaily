package com.aquadaily.app.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var preferences: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.switchNotifications.isChecked = preferences.isNotificationsEnabled()
        binding.switchSound.isChecked = preferences.isNotificationSoundEnabled()
        binding.switchVibration.isChecked = preferences.isNotificationVibrateEnabled()
        
        updateSubSettingsState(binding.switchNotifications.isChecked)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationsEnabled(isChecked)
            updateSubSettingsState(isChecked)
            // Here you could also trigger an immediate update to the WorkManager/AlarmManager if needed
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationSoundEnabled(isChecked)
        }

        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationVibrateEnabled(isChecked)
        }
    }

    private fun updateSubSettingsState(enabled: Boolean) {
        binding.switchSound.isEnabled = enabled
        binding.switchVibration.isEnabled = enabled
    }
}