package com.aquadaily.app.ui.history

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.databinding.ActivityAddHistoryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHistoryBinding
    private lateinit var preferences: PreferencesManager
    private var historyId: Int = 0

    private val viewModel: HistoryViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        HistoryViewModelFactory(
            HistoryRepository(database.historyDao()),
            PreferencesManager(applicationContext).getCurrentUserId()
        )
    }

    private val storageFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferencesManager(this)
        if (!preferences.isLoggedIn()) {
            finish()
            return
        }

        binding = ActivityAddHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyId = intent.getIntExtra("EXTRA_HISTORY_ID", 0)
        setupDateTimePickers()
        setupListeners()

        if (historyId != 0) {
            binding.tvAddTitle.text = "Edit History"
            binding.btnSaveHistory.text = "Update Record"
            loadHistoryData()
        }
    }

    private fun loadHistoryData() {
        lifecycleScope.launch {
            val history = viewModel.getHistoryById(historyId)
            history?.let {
                val date = storageFormatter.parse(it.date)
                binding.etDate.setText(if (date != null) displayFormatter.format(date) else it.date)
                binding.etTime.setText(it.time)
                binding.etAmount.setText(it.amount.toString())
                binding.etNote.setText(it.note)
            }
        }
    }

    private fun setupDateTimePickers() {
        val calendar = Calendar.getInstance()

        if (historyId == 0) {
            binding.etDate.setText(displayFormatter.format(calendar.time))
            binding.etTime.setText(
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)
                )
            )
        }

        binding.etDate.setOnClickListener {
            val dateStr = binding.etDate.text.toString()
            try {
                displayFormatter.parse(dateStr)?.let { calendar.time = it }
            } catch (_: Exception) {
            }

            DatePickerDialog(
                this,
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    binding.etDate.setText(displayFormatter.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etTime.setOnClickListener {
            val parts = binding.etTime.text.toString().split(":")
            val hour = if (parts.size == 2) parts[0].toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY) else calendar.get(Calendar.HOUR_OF_DAY)
            val minute = if (parts.size == 2) parts[1].toIntOrNull() ?: calendar.get(Calendar.MINUTE) else calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                this,
                { _, h, m -> binding.etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m)) },
                hour,
                minute,
                true
            ).show()
        }
    }

    private fun setupListeners() {
        binding.btnSaveHistory.setOnClickListener {
            val amount = binding.etAmount.text.toString().toIntOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Masukkan jumlah air yang valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val displayDate = binding.etDate.text.toString()
            val date = try { displayFormatter.parse(displayDate) } catch (_: Exception) { null }
            val storageDate = if (date != null) storageFormatter.format(date) else displayDate
            val userId = preferences.getCurrentUserId()

            val history = HistoryEntity(
                id = historyId,
                userId = userId,
                date = storageDate,
                time = binding.etTime.text.toString(),
                amount = amount,
                note = binding.etNote.text.toString()
            )

            if (historyId == 0) {
                viewModel.insert(history)
                Toast.makeText(this, "Data ditambahkan!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.update(history)
                Toast.makeText(this, "Data diperbarui!", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
