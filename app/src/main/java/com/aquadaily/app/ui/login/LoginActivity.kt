package com.aquadaily.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aquadaily.app.R
import com.aquadaily.app.core.auth.PasswordHasher
import com.aquadaily.app.core.database.AppDatabase
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.databinding.ActivityLoginBinding
import com.aquadaily.app.ui.auth.RegisterActivity
import com.aquadaily.app.ui.dashboard.DashboardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferences: PreferencesManager
    private val userRepository by lazy {
        com.aquadaily.app.core.repository.UserRepository(
            AppDatabase.getInstance(applicationContext).userDao()
        )
    }

    private val googleSignInLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account.email?.trim().orEmpty()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Google account has no email", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                lifecycleScope.launch {
                    val user = userRepository.getUserByEmail(email)
                    if (user == null || user.passwordHash.isEmpty()) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Account not registered. Create an account first.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        loginSuccess(user.id, user.name, user.email)
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = PreferencesManager(this)
        initView()
    }

    private fun initView() {
        supportActionBar?.hide()

        binding.btnSignIn.setOnClickListener {
            performLogin()
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnCreateAccount.setOnClickListener {
            navigateToRegister()
        }

        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = userRepository.getUserByEmail(email)

            when {
                user == null -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Account not found. Create an account first.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                user.passwordHash.isEmpty() -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "This account needs to be registered again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                user.passwordHash != PasswordHasher.hash(password) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Incorrect email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    loginSuccess(user.id, user.name, user.email)
                }
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Forgot Password")
        builder.setMessage("Password reset is not available for local accounts yet.")
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    private fun togglePasswordVisibility() {
        val currentlyVisible =
            binding.etPassword.inputType == InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        binding.etPassword.inputType = if (currentlyVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        binding.btnTogglePassword.alpha = if (currentlyVisible) 0.3f else 1f
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun loginSuccess(userId: Int, name: String, email: String) {
        preferences.setCurrentUserId(userId)
        preferences.setUserName(name)
        preferences.setEmail(email)
        preferences.setLoggedIn(true)

        startActivity(Intent(this, DashboardActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
