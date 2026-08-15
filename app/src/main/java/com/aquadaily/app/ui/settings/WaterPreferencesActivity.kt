package com.aquadaily.app.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityWaterPreferencesBinding
import java.util.Calendar

class WaterPreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaterPreferencesBinding
    private lateinit var preferences: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaterPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)

        initView()
        setupListeners()
    }

    private fun initView() {
        binding.etWaterTarget.setText(preferences.getWaterTarget().toString())
        binding.etInterval.setText(preferences.getReminderInterval().toString())
        binding.btnStartTime.text = "Start: ${preferences.getStartTime()}"
        binding.btnEndTime.text = "End: ${preferences.getEndTime()}"
    }

    private fun setupListeners() {
        binding.btnStartTime.setOnClickListener {
            showTimePicker(true)
        }

        binding.btnEndTime.setOnClickListener {
            showTimePicker(false)
        }

        binding.btnSave.setOnClickListener {
            savePreferences()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showTimePicker(isStart: Boolean) {
        val currentTime = if (isStart) preferences.getStartTime() else preferences.getEndTime()
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(this, { _, h, m ->
            val time = String.format("%02d:%02d", h, m)
            if (isStart) {
                binding.btnStartTime.text = "Start: $time"
                preferences.setStartTime(time)
            } else {
                binding.btnEndTime.text = "End: $time"
                preferences.setEndTime(time)
            }
        }, hour, minute, true).show()
    }

    private fun savePreferences() {
        val target = binding.etWaterTarget.text.toString().toIntOrNull() ?: 2000
        val interval = binding.etInterval.text.toString().toIntOrNull() ?: 120

        if (target <= 0) {
            binding.inputWaterTarget.error = "Target must be greater than 0"
            return
        }
        binding.inputWaterTarget.error = null

        if (interval <= 0) {
            binding.inputInterval.error = "Interval must be greater than 0"
            return
        }
        binding.inputInterval.error = null

        preferences.setWaterTarget(target)
        preferences.setReminderInterval(interval)

        Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
