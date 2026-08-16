package com.aquadaily.app.ui.settings

import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var preferences: PreferencesManager
    private var previewRingtone: Ringtone? = null

    companion object {
        private const val REQUEST_NOTIFICATION_SOUND = 2001
    }

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
        binding.tvSelectedSound.text = preferences.getNotificationSoundName()
        updateSubSettingsState(binding.switchNotifications.isChecked)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            stopPreview()
            finish()
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationsEnabled(isChecked)
            updateSubSettingsState(isChecked)
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationSoundEnabled(isChecked)
            updateSubSettingsState(binding.switchNotifications.isChecked)
        }

        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            preferences.setNotificationVibrateEnabled(isChecked)
        }

        binding.btnChooseSound.setOnClickListener {
            openSoundPicker()
        }

        binding.btnPreviewSound.setOnClickListener {
            previewSelectedSound()
        }

        binding.btnDefaultSound.setOnClickListener {
            stopPreview()
            preferences.setNotificationSoundUri(null)
            preferences.setNotificationSoundName("Suara bawaan")
            binding.tvSelectedSound.text = "Suara bawaan"
            Toast.makeText(this, "Suara dikembalikan ke bawaan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSubSettingsState(enabled: Boolean) {
        val soundEnabled = enabled && binding.switchSound.isChecked
        binding.switchSound.isEnabled = enabled
        binding.switchVibration.isEnabled = enabled
        binding.btnChooseSound.isEnabled = soundEnabled
        binding.btnPreviewSound.isEnabled = soundEnabled
        binding.btnDefaultSound.isEnabled = enabled
    }

    private fun openSoundPicker() {
        stopPreview()

        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_NOTIFICATION
            )
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_TITLE,
                "Pilih suara notifikasi"
            )
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                false
            )
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                preferences.getNotificationSoundUri()?.let { android.net.Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
        }

        startActivityForResult(intent, REQUEST_NOTIFICATION_SOUND)
    }

    @Deprecated("Deprecated in Android API; kept for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_NOTIFICATION_SOUND || resultCode != RESULT_OK) {
            return
        }

        val selectedUri = data?.getParcelableExtra<android.net.Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

        if (selectedUri == null) {
            return
        }

        val ringtone = RingtoneManager.getRingtone(this, selectedUri)
        val title = ringtone?.getTitle(this)?.takeIf { it.isNotBlank() } ?: "Suara pilihan"

        preferences.setNotificationSoundUri(selectedUri.toString())
        preferences.setNotificationSoundName(title)
        binding.tvSelectedSound.text = title

        Toast.makeText(this, "Suara disimpan", Toast.LENGTH_SHORT).show()
    }

    private fun previewSelectedSound() {
        stopPreview()

        val uri = preferences.getNotificationSoundUri()?.let { android.net.Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        previewRingtone = RingtoneManager.getRingtone(this, uri)
        previewRingtone?.play()
    }

    private fun stopPreview() {
        previewRingtone?.stop()
        previewRingtone = null
    }

    override fun onPause() {
        stopPreview()
        super.onPause()
    }

    override fun onDestroy() {
        stopPreview()
        super.onDestroy()
    }
}
