package com.aquadaily.app.ui.dashboard

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.entity.UserEntity
import com.aquadaily.app.core.notification.NotificationHelper
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository
import com.aquadaily.app.databinding.ActivityDashboardBinding
import com.aquadaily.app.databinding.ItemScheduleBinding
import com.aquadaily.app.animation.BottomNavAnimation
import com.aquadaily.app.animation.DashboardAnimation
import com.aquadaily.app.ui.settings.SettingsActivity
import com.bumptech.glide.Glide
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var preferences: PreferencesManager
    private lateinit var scheduleAdapter: ScheduleAdapter

    private var lastProgress = 0
    private var lastObservedWater: Int? = null

    private val viewModel: DashboardViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val historyRepository = HistoryRepository(database.historyDao())
        val reminderRepository = ReminderRepository(database.reminderDao())
        DashboardViewModelFactory(historyRepository, reminderRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)

        setupRecyclerView()
        initView()
        initListener()
        observeViewModel()
        setupAnimations()
    }

    private fun setupAnimations() {
        DashboardAnimation.animateCard(binding.cvGoal, 0L)
        DashboardAnimation.animateCard(binding.tvQuickAddLabel, 100L)
        DashboardAnimation.animateCard(binding.llQuickAdd, 200L)
        DashboardAnimation.animateCard(binding.cvReminder, 300L)
        DashboardAnimation.animateCard(binding.llScheduleHeader, 400L)
        DashboardAnimation.animateCard(binding.rvSchedule, 500L)
    }

    override fun onResume() {
        super.onResume()
        updateProfileInfo()
        binding.bottomNavigation.selectedItemId = com.aquadaily.app.R.id.nav_home

        val homeItem = binding.bottomNavigation.findViewById<View>(com.aquadaily.app.R.id.nav_home)
        homeItem.post {
            BottomNavAnimation.animateNavigationItem(homeItem)
        }
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter()
        binding.rvSchedule.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = scheduleAdapter
        }
    }

    private fun initView() {
        updateProfileInfo()

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when (hour) {
            in 0..11 -> "Good Morning 👋"
            in 12..16 -> "Good Afternoon 👋"
            else -> "Good Evening 👋"
        }
    }

    private fun updateProfileInfo() {
        binding.tvUserName.text = preferences.getUserName()
        val target = preferences.getWaterTarget()
        binding.tvTargetGoal.text = " / $target"
        viewModel.todayWaterIntake.value?.let { updateProgress(it) }
    }

    private fun observeViewModel() {
        val database = AppDatabase.getInstance(this)
        val userRepository = com.aquadaily.app.core.repository.UserRepository(database.userDao())

        userRepository.getUser().asLiveData().observe(this) { user: UserEntity? ->
            if (user != null) {
                binding.tvUserName.text = user.name
                if (!user.profileImage.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(Uri.parse(user.profileImage))
                        .placeholder(com.aquadaily.app.R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.ivUserAvatar)
                } else {
                    binding.ivUserAvatar.setImageResource(com.aquadaily.app.R.drawable.ic_person)
                }
            }
        }

        viewModel.todayWaterIntake.observe(this) { total ->
            val amount = total ?: 0
            val previousAmount = lastObservedWater
            lastObservedWater = amount

            if (previousAmount != null && amount > previousAmount) {
                // Water intake was recorded: release persistent reminder alerts.
                NotificationHelper.cancelActiveReminderNotifications(this)
            }

            updateProgress(amount)
        }

        viewModel.todayHistory.observe(this) { history ->
            scheduleAdapter.submitList(history.take(5))
        }

        viewModel.reminders.observe(this) { reminders ->
            reminders.firstOrNull { it.isEnabled }?.let { next ->
                binding.tvNextReminderTime.text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d • Drink %d ml",
                    next.hour,
                    next.minute,
                    next.amount
                )
            }
        }
    }

    private fun updateProgress(amount: Int) {
        binding.tvCurrentProgress.text = "$amount"
        val goal = preferences.getWaterTarget()
        val targetProgress = if (goal > 0) {
            (amount.toFloat() / goal * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        animateProgress(targetProgress)

        val remaining = goal - amount
        binding.tvRemaining.text = if (remaining > 0) {
            "$remaining ml remaining"
        } else {
            NotificationHelper.cancelActiveReminderNotifications(this)
            "Goal reached!"
        }
    }

    private fun animateProgress(targetProgress: Int) {
        val animator = ValueAnimator.ofInt(lastProgress, targetProgress)
        animator.duration = 1000L
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            binding.pbHorizontal.progress = progress
            binding.pbCircular.progress = progress
            binding.tvPercentage.text = "$progress%"
        }
        animator.start()
        lastProgress = targetProgress
    }

    private fun initListener() {
        binding.ivUserAvatar.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Match the labels on the UI.
        binding.btn100ml.setOnClickListener { viewModel.addWater(100) }
        binding.btn250ml.setOnClickListener { viewModel.addWater(250) }
        binding.btn500ml.setOnClickListener { viewModel.addWater(500) }

        binding.btnSeeAll.setOnClickListener {
            startActivity(Intent(this, com.aquadaily.app.ui.reminder.ReminderActivity::class.java))
        }

        binding.cvReminder.setOnClickListener {
            startActivity(Intent(this, com.aquadaily.app.ui.reminder.ReminderActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) {
                return@setOnItemSelectedListener false
            }

            val itemView = binding.bottomNavigation.findViewById<View>(item.itemId)
            BottomNavAnimation.animateClick(itemView)

            when (item.itemId) {
                com.aquadaily.app.R.id.nav_home -> true
                com.aquadaily.app.R.id.nav_schedule -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.reminder.ReminderActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_right, com.aquadaily.app.R.anim.slide_out_left)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_history -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.history.HistoryActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_right, com.aquadaily.app.R.anim.slide_out_left)
                    finish()
                    true
                }
                com.aquadaily.app.R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(com.aquadaily.app.R.anim.slide_in_right, com.aquadaily.app.R.anim.slide_out_left)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    inner class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {
        private var items = listOf<HistoryEntity>()

        fun submitList(newItems: List<HistoryEntity>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
            DashboardAnimation.animateRecyclerViewItem(holder.itemView, position)
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(private val itemBinding: ItemScheduleBinding) : RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(item: HistoryEntity) {
                itemBinding.tvTime.text = item.time
                itemBinding.tvAmount.text = "${item.amount} ml"
                itemBinding.ivStatus.setImageResource(com.aquadaily.app.R.drawable.ic_water_drop)
            }
        }
    }
}
