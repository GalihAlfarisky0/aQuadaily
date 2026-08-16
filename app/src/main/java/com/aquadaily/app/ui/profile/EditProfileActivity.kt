package com.aquadaily.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.database.entity.UserEntity
import com.aquadaily.app.core.repository.HistoryRepository
import com.aquadaily.app.core.repository.ReminderRepository
import com.aquadaily.app.core.repository.UserRepository
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityEditProfileBinding
import com.aquadaily.app.ui.settings.SettingsViewModel
import com.aquadaily.app.ui.settings.SettingsViewModelFactory
import com.bumptech.glide.Glide

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: SettingsViewModel
    private var currentUser: UserEntity? = null
    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val imageUri = result.data?.data ?: return@registerForActivityResult
        selectedImageUri = imageUri

        try {
            contentResolver.takePersistableUriPermission(
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Glide.with(this)
            .load(imageUri)
            .circleCrop()
            .into(binding.ivProfile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = PreferencesManager(this)
        val userId = preferences.getCurrentUserId()
        if (userId <= 0 || !preferences.isLoggedIn()) {
            finish()
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

        setupGenderSpinner()
        setupObservers()
        setupListeners()
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            genders
        )
        binding.actvGender.setAdapter(adapter)
    }

    private fun setupObservers() {
        viewModel.user.observe(this, Observer { user: UserEntity? ->
            if (user == null) return@Observer

            currentUser = user
            binding.etName.setText(user.name)
            binding.etEmail.setText(user.email)
            binding.actvGender.setText(user.gender, false)
            binding.etAge.setText(user.age.toString())
            binding.etWeight.setText(user.weight.toString())

            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(this@EditProfileActivity)
                    .load(Uri.parse(user.profileImage))
                    .placeholder(com.aquadaily.app.R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.ivProfile)
            }
        })
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selectImageLauncher.launch(intent)
        }
    }

    private fun saveProfile() {
        val user = currentUser ?: return

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val gender = binding.actvGender.text.toString()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isEmpty()) {
            binding.inputName.error = "Name cannot be empty"
            return
        }
        binding.inputName.error = null

        if (email.isEmpty()) {
            binding.inputEmail.error = "Email cannot be empty"
            return
        }
        binding.inputEmail.error = null

        val updatedUser = user.copy(
            name = name,
            email = email,
            gender = gender,
            age = age,
            weight = weight,
            profileImage = selectedImageUri?.toString() ?: user.profileImage
        )

        viewModel.updateUser(updatedUser)

        val preferences = PreferencesManager(this)
        preferences.setUserName(name)
        preferences.setEmail(email)

        Toast.makeText(
            this,
            "Profile updated successfully",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }
}
