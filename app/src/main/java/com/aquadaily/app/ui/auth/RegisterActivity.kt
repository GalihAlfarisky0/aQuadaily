package com.aquadaily.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.databinding.ActivityRegisterBinding
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.ui.dashboard.DashboardActivity
import com.aquadaily.app.ui.login.LoginActivity

import com.aquadaily.app.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferences: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = PreferencesManager(this)

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
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Simpan data user sederhana ke Preferences
        preferences.setUserName(name)
        preferences.setEmail(email)
        preferences.setLoggedIn(true)

        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
