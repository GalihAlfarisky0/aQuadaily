package com.aquadaily.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aquadaily.app.R
import com.aquadaily.app.core.auth.PasswordHasher
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.database.entity.UserEntity
import com.aquadaily.app.core.repository.UserRepository
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityRegisterBinding
import com.aquadaily.app.ui.dashboard.DashboardActivity
import com.aquadaily.app.ui.login.LoginActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferences: PreferencesManager
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)
        userRepository = UserRepository(
            AppDatabase.getInstance(applicationContext).userDao()
        )

        initView()
    }

    private fun initView() {
        supportActionBar?.hide()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val existing = userRepository.getUserByEmail(email)

            if (existing != null && existing.passwordHash.isNotEmpty()) {
                Toast.makeText(
                    this@RegisterActivity,
                    "An account with this email already exists",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val savedUser = if (existing != null) {
                existing.copy(
                    name = name,
                    email = email,
                    passwordHash = PasswordHasher.hash(password)
                )
            } else {
                UserEntity(
                    name = name,
                    email = email,
                    passwordHash = PasswordHasher.hash(password),
                    gender = "Male",
                    age = 0,
                    weight = 0.0
                )
            }

            val userId = if (existing != null) {
                userRepository.updateUser(savedUser)
                existing.id
            } else {
                userRepository.insertUser(savedUser).toInt()
            }

            preferences.setCurrentUserId(userId)
            preferences.setUserName(name)
            preferences.setEmail(email)
            preferences.setLoggedIn(true)

            Toast.makeText(
                this@RegisterActivity,
                "Registration successful",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this@RegisterActivity, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
}
