package com.aquadaily.app.ui.reminder

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aquadaily.app.App
import com.aquadaily.app.animation.BottomNavAnimation
import com.aquadaily.app.animation.DashboardAnimation
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityReminderBinding
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback

class ReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReminderBinding
    private lateinit var preferences: PreferencesManager

    private val viewModel: ReminderViewModel by viewModels {
        val app = application as App
        ReminderViewModel.Factory(
            app.reminderRepository,
            app.alarmScheduler,
            PreferencesManager(applicationContext).getCurrentUserId()
        )
    }

    private lateinit var adapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)

        preferences = PreferencesManager(this)
        if (!preferences.isLoggedIn()) {
            finish()
            return
        }

        binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        observeData()
        initListener()
        setupBottomNavigation()
        setupAnimations()
    }

    private fun setupAnimations() {
        DashboardAnimation.animateCard(binding.tvReminderCount, 0L)
        DashboardAnimation.animateCard(binding.tvActiveCount, 100L)
        DashboardAnimation.animateCard(binding.recyclerReminder, 200L)
        DashboardAnimation.animateCard(binding.fabAddReminder, 300L)
    }

    private fun initRecyclerView() {
        adapter = ReminderAdapter(
            onToggle = { reminder, isEnabled -> viewModel.updateReminderStatus(reminder, isEnabled) },
            onEdit = { reminder ->
                startActivity(Intent(this, AddReminderActivity::class.java).apply {
                    putExtra("REMINDER_ID", reminder.id)
                })
            },
            onDelete = { reminder -> viewModel.deleteReminder(reminder) }
        )

        binding.recyclerReminder.layoutManager = LinearLayoutManager(this)
        binding.recyclerReminder.adapter = adapter
    }

    private fun observeData() {
        viewModel.allReminders.observe(this) { reminders ->
            if (reminders.isNullOrEmpty()) {
                binding.recyclerReminder.visibility = View.GONE
                binding.tvReminderCount.text = "0 Reminders"
                binding.tvActiveCount.text = "0 Active"
            } else {
                binding.recyclerReminder.visibility = View.VISIBLE
                adapter.submitList(reminders)
                binding.tvReminderCount.text = "${reminders.size} Reminders"
                binding.tvActiveCount.text = "${reminders.count { it.isEnabled }} Active"
            }
        }
    }

    private fun initListener() {
        binding.fabAddReminder.setOnClickListener {
            val intent = Intent(this, AddReminderActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                binding.fabAddReminder,
                "shared_element_container"
            )
            startActivity(intent, options.toBundle())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = com.aquadaily.app.R.id.nav_schedule

        val scheduleItem = binding.bottomNavigation.findViewById<View>(com.aquadaily.app.R.id.nav_schedule)
        scheduleItem.post { BottomNavAnimation.animateNavigationItem(scheduleItem) }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnItemSelectedListener false

            val itemView = binding.bottomNavigation.findViewById<View>(item.itemId)
            BottomNavAnimation.animateClick(itemView)

            when (item.itemId) {
                com.aquadaily.app.R.id.nav_home -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.dashboard.DashboardActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_left, com.aquadaily.app.R.anim.slide_out_right)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_schedule -> true
                com.aquadaily.app.R.id.nav_history -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.history.HistoryActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_right, com.aquadaily.app.R.anim.slide_out_left)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_settings -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.settings.SettingsActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_right, com.aquadaily.app.R.anim.slide_out_left)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
