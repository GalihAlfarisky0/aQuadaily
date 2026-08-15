package com.aquadaily.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aquadaily.app.ui.login.LoginActivity
import com.aquadaily.app.ui.dashboard.DashboardActivity
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.R

import com.aquadaily.app.animation.SplashAnimation
import com.aquadaily.app.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        SplashAnimation.play(binding) {
            navigateToNextScreen()
        }
    }

    override fun onDestroy() {
        SplashAnimation.stop()
        super.onDestroy()
    }

    private fun navigateToNextScreen() {
        val preferences = PreferencesManager(this)
        
        if (preferences.isLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}