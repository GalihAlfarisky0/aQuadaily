package com.aquadaily.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.database.entity.UserEntity
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository
import com.aquadaily.app.core.repository.UserRepository
import com.aquadaily.app.animation.DashboardAnimation
import com.aquadaily.app.animation.BottomNavAnimation
import com.aquadaily.app.databinding.ActivitySettingsBinding
import com.aquadaily.app.ui.login.LoginActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferences: PreferencesManager
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)
        val userId = preferences.getCurrentUserId()
        if (userId <= 0 || !preferences.isLoggedIn()) {
            logout()
            return
        }

        val database = AppDatabase.getInstance(this)
        val userRepository = UserRepository(database.userDao())
        val historyRepository = HistoryRepository(database.historyDao())
        val reminderRepository = ReminderRepository(database.reminderDao())
        val factory = SettingsViewModelFactory(
            userRepository,
            historyRepository,
            reminderRepository,
            userId
        )
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setupObservers()
        setupListeners()
        setupAnimations()
    }

    private fun setupAnimations() {
        DashboardAnimation.animateCard(binding.cardProfile, 0L)
    }

    private fun setupObservers() {
        viewModel.user.observe(this, Observer { user: UserEntity? ->
            if (user != null) {
                displayUser(user)
            } else {
                logout()
            }
        })

        viewModel.dailyWater.observe(this, Observer { history ->
            if (history != null) {
                viewModel.calculateStreak(history, preferences.getWaterTarget())
            }
        })

        viewModel.streak.observe(this, Observer { streak: Int? ->
            binding.tvStreak.text = "● ${streak ?: 0}-day streak 🔥"
        })
    }

    private fun displayUser(user: UserEntity) {
        binding.tvProfileName.text = user.name
        binding.tvProfileEmail.text = user.email
        binding.badgeGender.text = if (user.gender == "Female") "♀ Female" else "♂ Male"
        binding.badgeAge.text = "🎂 ${user.age} yrs"
        binding.badgeWeight.text = "⚖ ${user.weight.toInt()} kg"

        if (!user.profileImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(user.profileImage))
                .placeholder(com.aquadaily.app.R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(com.aquadaily.app.R.drawable.ic_person)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::preferences.isInitialized && preferences.isLoggedIn()) {
            updateSummaries()
        }
    }

    private fun updateSummaries() {
        binding.tvTargetStatus.text = "Target: ${preferences.getWaterTarget()} ml"

        val notifStatus = if (preferences.isNotificationsEnabled()) "Enabled" else "Disabled"
        binding.tvNotifStatus.text = "$notifStatus · All days"

        val database = AppDatabase.getInstance(this)
        database.reminderDao()
            .getAllReminders(preferences.getCurrentUserId())
            .asLiveData()
            .observe(this, Observer { reminders ->
                val activeCount = reminders?.count { it.isEnabled } ?: 0
                binding.tvActiveReminders.text = "$activeCount active reminders"
            })
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener { openEditProfile() }

        binding.menuWater.setOnClickListener {
            startActivity(Intent(this, WaterPreferencesActivity::class.java))
        }

        binding.menuNotification.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        binding.menuSchedule.setOnClickListener {
            startActivity(Intent(this, com.aquadaily.app.ui.reminder.ReminderActivity::class.java))
        }

        binding.menuHistory.setOnClickListener {
            startActivity(Intent(this, com.aquadaily.app.ui.history.HistoryActivity::class.java))
        }

        binding.menuFeedback.setOnClickListener { showFeedbackDialog() }

        binding.menuRate.setOnClickListener {
            Toast.makeText(this, "Thank you for rating!", Toast.LENGTH_SHORT).show()
        }

        binding.menuReset.setOnClickListener { showResetDataDialog() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = com.aquadaily.app.R.id.nav_settings

        val settingsItem = binding.bottomNavigation.findViewById<android.view.View>(com.aquadaily.app.R.id.nav_settings)
        settingsItem.post { BottomNavAnimation.animateNavigationItem(settingsItem) }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnItemSelectedListener false

            val itemView = binding.bottomNavigation.findViewById<android.view.View>(item.itemId)
            BottomNavAnimation.animateClick(itemView)

            when (item.itemId) {
                com.aquadaily.app.R.id.nav_home -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.dashboard.DashboardActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_left, com.aquadaily.app.R.anim.slide_out_right)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_schedule -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.reminder.ReminderActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_left, com.aquadaily.app.R.anim.slide_out_right)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_history -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.history.HistoryActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_left, com.aquadaily.app.R.anim.slide_out_right)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun openEditProfile() {
        startActivity(Intent(this, com.aquadaily.app.ui.profile.EditProfileActivity::class.java))
    }

    private fun showFeedbackDialog() {
        val input = TextInputEditText(this)
        input.hint = getString(com.aquadaily.app.R.string.write_feedback_hint)

        val container = FrameLayout(this)
        container.setPadding(40, 10, 40, 0)
        container.addView(input, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.aquadaily.app.R.string.feedback))
            .setView(container)
            .setNegativeButton(getString(com.aquadaily.app.R.string.cancel), null)
            .setPositiveButton(getString(com.aquadaily.app.R.string.send)) { _, _ ->
                val feedback = input.text.toString()
                if (feedback.isNotBlank()) {
                    sendFeedbackEmail(feedback)
                } else {
                    Toast.makeText(this, getString(com.aquadaily.app.R.string.feedback_empty_error), Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun sendFeedbackEmail(feedback: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@aquadaily.com"))
            putExtra(Intent.EXTRA_SUBJECT, "aQuaDaily Feedback")
            putExtra(Intent.EXTRA_TEXT, feedback)
        }
        try {
            startActivity(Intent.createChooser(intent, getString(com.aquadaily.app.R.string.send)))
            Toast.makeText(this, getString(com.aquadaily.app.R.string.feedback_success), Toast.LENGTH_SHORT).show()
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, getString(com.aquadaily.app.R.string.no_email_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.aquadaily.app.R.string.about_aquadaily))
            .setMessage(getString(com.aquadaily.app.R.string.about_desc))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.aquadaily.app.R.string.log_out))
            .setMessage(getString(com.aquadaily.app.R.string.logout_confirm))
            .setNegativeButton(getString(com.aquadaily.app.R.string.cancel), null)
            .setPositiveButton(getString(com.aquadaily.app.R.string.log_out)) { _, _ -> logout() }
            .show()
    }

    private fun showResetDataDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.aquadaily.app.R.string.reset_data_title))
            .setMessage(getString(com.aquadaily.app.R.string.reset_data_message))
            .setNegativeButton(getString(com.aquadaily.app.R.string.cancel), null)
            .setPositiveButton(getString(com.aquadaily.app.R.string.clear_all_data)) { _, _ ->
                viewModel.clearAllData()
                Toast.makeText(this, getString(com.aquadaily.app.R.string.reset_data_success), Toast.LENGTH_SHORT).show()
                logout()
            }
            .show()
    }

    private fun logout() {
        if (::preferences.isInitialized) {
            preferences.clearSession()
        }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
