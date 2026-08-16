package com.aquadaily.app.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.R
import com.aquadaily.app.core.notification.NotificationHelper
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var preferences: PreferencesManager

    private val ringtoneKeys = arrayOf(
        PreferencesManager.RINGTONE_DEFAULT,
        PreferencesManager.RINGTONE_AQUADAILY,
        PreferencesManager.RINGTONE_BRAND_NEW_DAY
    )

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

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.notification_ringtone_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerRingtone.adapter = adapter

        val currentIndex = ringtoneKeys.indexOf(
            preferences.getNotificationRingtone()
        ).coerceAtLeast(0)

        binding.spinnerRingtone.setSelection(currentIndex)
        updateSubSettingsState(binding.switchNotifications.isChecked)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationsEnabled(isChecked)
            updateSubSettingsState(isChecked)
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationSoundEnabled(isChecked)
            refreshNotificationChannels()
        }

        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationVibrateEnabled(isChecked)
            refreshNotificationChannels()
        }

        binding.spinnerRingtone.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    preferences.setNotificationRingtone(
                        ringtoneKeys[position]
                    )
                    refreshNotificationChannels()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) = Unit
            }
    }

    private fun updateSubSettingsState(enabled: Boolean) {
        binding.switchSound.isEnabled = enabled
        binding.switchVibration.isEnabled = enabled
        binding.spinnerRingtone.isEnabled = enabled
    }

    private fun refreshNotificationChannels() {
        NotificationHelper(this).createNotificationChannels()
    }
}