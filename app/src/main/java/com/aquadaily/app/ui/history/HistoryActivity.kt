package com.aquadaily.app.ui.history

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aquadaily.app.R
import com.aquadaily.app.animation.DashboardAnimation
import com.aquadaily.app.animation.BottomNavAnimation
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.database.model.MonthlyWater
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.databinding.ActivityHistoryBinding
import com.aquadaily.app.ui.dashboard.DashboardActivity
import com.aquadaily.app.ui.settings.SettingsActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var preferences: PreferencesManager
    private lateinit var historyRecordAdapter: HistoryRecordAdapter

    private val viewModel: HistoryViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = HistoryRepository(database.historyDao())
        HistoryViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)
        
        setupRecyclerView()
        setupBottomNavigation()
        setupPeriodSwitcher()
        observeData()
        setupAnimations()
    }

    private fun setupAnimations() {
        DashboardAnimation.animateCard(binding.tvTitle, 0L)
        DashboardAnimation.animateCard(binding.tvSubtitle, 50L)
        DashboardAnimation.animateCard(binding.layoutStats, 100L)
        DashboardAnimation.animateCard(binding.cvAnalysis, 200L)
        DashboardAnimation.animateCard(binding.tvLabelRecords, 300L)
        DashboardAnimation.animateCard(binding.recyclerHistory, 400L)
    }

    private fun setupPeriodSwitcher() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener { _, checkedIds ->
            val isWeekly = checkedIds.contains(R.id.chipWeekly)
            binding.tvChartTitle.text = if (isWeekly) getString(R.string.weekly_overview) else getString(R.string.monthly_overview)
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.getDailyWater().value?.let { dailyData ->
            if (binding.chipWeekly.isChecked) {
                setupWeeklyChart(dailyData.takeLast(7))
            }
        }
        
        viewModel.getMonthlyWater().value?.let { monthlyData ->
            if (binding.chipMonthly.isChecked) {
                setupMonthlyChart(monthlyData)
            }
        }
    }

    private fun setupRecyclerView() {
        historyRecordAdapter = HistoryRecordAdapter(preferences.getWaterTarget())
        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyRecordAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeData() {
        val dailyTarget = preferences.getWaterTarget()
        
        viewModel.getDailyWater().observe(this) { dailyData ->
            if (dailyData != null && dailyData.isNotEmpty()) {
                historyRecordAdapter.submitList(dailyData.reversed())
                updateStats(dailyData, dailyTarget)
                viewModel.calculateStreak(dailyData, dailyTarget)
                
                if (binding.chipWeekly.isChecked) {
                    setupWeeklyChart(dailyData.takeLast(7))
                }
            }
        }

        viewModel.getMonthlyWater().observe(this) { monthlyData ->
            if (monthlyData != null && monthlyData.isNotEmpty() && binding.chipMonthly.isChecked) {
                setupMonthlyChart(monthlyData)
            }
        }

        viewModel.streak.observe(this) { streak ->
            binding.tvStreakStats.text = "$streak Days"
        }
    }

    private fun updateStats(data: List<DailyWater>, target: Int) {
        val avg = if (data.isNotEmpty()) data.map { it.totalAmount }.average().toInt() else 0
        val best = data.maxOfOrNull { it.totalAmount } ?: 0
        val worst = data.minOfOrNull { it.totalAmount } ?: 0
        val total = data.sumOf { it.totalAmount }
        
        binding.tvAvgDaily.text = "$avg ml"
        binding.tvBestDay.text = "$best ml"
        binding.tvTotalIntake.text = "$total ml"
        binding.tvWorstDay.text = "$worst ml"
        
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayData = data.find { it.date == todayStr }?.totalAmount ?: 0
        val completion = if (target > 0) (todayData.toFloat() / target * 100).toInt() else 0
        binding.tvCompletion.text = "$completion%"
    }

    private fun setupWeeklyChart(data: List<DailyWater>) {
        val entries = data.mapIndexed { index, dailyWater ->
            Entry(index.toFloat(), dailyWater.totalAmount.toFloat())
        }

        val dataSet = LineDataSet(entries, "Water Intake")
        dataSet.apply {
            color = Color.parseColor("#2196F3")
            setCircleColor(Color.parseColor("#2196F3"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleRadius = 3f
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = Color.parseColor("#2196F3")
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        binding.mainChart.apply {
            this.data = lineData
            description.isEnabled = false
            legend.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(data.map { formatDateShort(it.date) })
                granularity = 1f
                textColor = Color.parseColor("#9E9E9E")
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textColor = Color.parseColor("#9E9E9E")
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupMonthlyChart(data: List<MonthlyWater>) {
        val entries = data.mapIndexed { index, monthlyWater ->
            Entry(index.toFloat(), monthlyWater.totalAmount.toFloat())
        }

        val dataSet = LineDataSet(entries, "Monthly Intake")
        dataSet.apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawCircleHole(true)
            circleHoleRadius = 3f
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = Color.parseColor("#4CAF50")
            fillAlpha = 30
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        binding.mainChart.apply {
            this.data = lineData
            xAxis.valueFormatter = IndexAxisValueFormatter(data.map { formatMonth(it.month) })
            animateY(1000)
            invalidate()
        }
    }

    private fun formatMonth(monthStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val date = inputFormat.parse(monthStr)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            monthStr
        }
    }

    private fun formatDateShort(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_history
        
        val historyItem = binding.bottomNavigation.findViewById<android.view.View>(R.id.nav_history)
        historyItem.post {
            BottomNavAnimation.animateNavigationItem(historyItem)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnItemSelectedListener false

            val itemView = binding.bottomNavigation.findViewById<android.view.View>(item.itemId)
            BottomNavAnimation.animateClick(itemView)

            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                    true
                }
                R.id.nav_schedule -> {
                    startActivity(Intent(this, com.aquadaily.app.ui.reminder.ReminderActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                    true
                }
                R.id.nav_history -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}