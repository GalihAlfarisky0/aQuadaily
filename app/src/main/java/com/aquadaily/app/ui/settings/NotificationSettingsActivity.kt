package com.aquadaily.app.ui.settings

import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.core.notification.NotificationSoundCatalog
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityNotificationSettingsBinding

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var preferences: PreferencesManager

    private var previewRingtone: Ringtone? = null
    private var previewPlayer: MediaPlayer? = null

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

    /**
     * Shows sounds bundled inside AquaDaily's res/raw folder.
     * The system ringtone picker cannot reliably expose app-local raw resources,
     * so the app now owns this picker instead of delegating to the OS.
     */
    private fun openSoundPicker() {
        stopPreview()

        val sounds = listOf(
            NotificationSoundCatalog.Sound(
                id = NotificationSoundCatalog.DEFAULT_ID,
                name = "Suara bawaan",
                resourceId = 0,
            ),
        ) + NotificationSoundCatalog.getBundledSounds()

        val currentId = preferences.getNotificationSoundUri()
            ?.removePrefix("aquadaily://sound/")
            ?: NotificationSoundCatalog.DEFAULT_ID

        val names = sounds.map { it.name }.toTypedArray()
        val checkedIndex = sounds.indexOfFirst { it.id == currentId }.coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("Pilih suara notifikasi")
            .setSingleChoiceItems(names, checkedIndex) { dialog, which ->
                val selected = sounds[which]

                if (selected.id == NotificationSoundCatalog.DEFAULT_ID) {
                    preferences.setNotificationSoundUri(null)
                } else {
                    preferences.setNotificationSoundUri(
                        "aquadaily://sound/${selected.id}"
                    )
                }

                preferences.setNotificationSoundName(selected.name)
                binding.tvSelectedSound.text = selected.name
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Suara ${selected.name} dipilih",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun previewSelectedSound() {
        stopPreview()

        val selectedId = preferences.getNotificationSoundUri()
            ?.removePrefix("aquadaily://sound/")

        val bundledSound = NotificationSoundCatalog.findById(selectedId)
        if (bundledSound != null) {
            previewPlayer = MediaPlayer.create(this, bundledSound.resourceId)?.apply {
                setOnCompletionListener { stopPreview() }
                start()
            }
            return
        }

        val systemUri = preferences.getNotificationSoundUri()
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        previewRingtone = RingtoneManager.getRingtone(this, systemUri)
        previewRingtone?.play()
    }

    private fun stopPreview() {
        previewRingtone?.stop()
        previewRingtone = null

        previewPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (_: IllegalStateException) {
                // Player may already have completed or been released.
            }
            player.release()
        }
        previewPlayer = null
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
