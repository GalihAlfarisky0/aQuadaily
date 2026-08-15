package com.aquadaily.app.ui.reminder

import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.App
import com.aquadaily.app.core.database.entity.ReminderEntity
import com.aquadaily.app.databinding.ActivityAddReminderBinding
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private val viewModel: ReminderViewModel by viewModels {
        val app = application as App
        ReminderViewModel.Factory(app.reminderRepository, app.alarmScheduler)
    }

    private var reminderId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup transitions after binding is initialized
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(binding.scrollView)
            duration = 400L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(binding.scrollView)
            duration = 300L
        }

        reminderId = intent.getIntExtra("REMINDER_ID", -1)
        if (reminderId != -1) {
            binding.tvTitle.text = getString(com.aquadaily.app.R.string.edit_reminder)
            binding.tvSubtitle.text = getString(com.aquadaily.app.R.string.update_schedule)
            binding.btnSave.text = getString(com.aquadaily.app.R.string.update_reminder)
            loadReminderData()
        }

        initListener()
    }

    private fun loadReminderData() {
        viewModel.allReminders.observe(this) { reminders ->
            val reminder = reminders.find { it.id == reminderId }
            reminder?.let {
                binding.timePicker.hour = it.hour
                binding.timePicker.minute = it.minute
                binding.etAmount.setText(it.amount.toString())
                
                val daysArray = resources.getStringArray(com.aquadaily.app.R.array.days_array)
                val dayIndex = daysArray.indexOf(it.day)
                if (dayIndex >= 0) {
                    binding.spinnerDay.setSelection(dayIndex)
                }
            }
        }
    }

    private fun initListener() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {
            saveReminder()
        }
    }

    private fun saveReminder() {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute
        val day = binding.spinnerDay.selectedItem.toString()
        val amountStr = binding.etAmount.text.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter water amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toIntOrNull() ?: 0
        val reminder = ReminderEntity(
            id = if (reminderId != -1) reminderId else 0,
            hour = hour,
            minute = minute,
            day = day,
            amount = amount,
            isEnabled = true
        )

        if (reminderId != -1) {
            viewModel.updateReminder(reminder)
            Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertReminder(reminder)
            Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}