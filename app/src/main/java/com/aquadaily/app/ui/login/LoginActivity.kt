package com.aquadaily.app.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aquadaily.app.databinding.ActivityLoginBinding
import com.aquadaily.app.core.preferences.PreferencesManager
import com.aquadaily.app.ui.dashboard.DashboardActivity

import android.widget.Toast
import com.aquadaily.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.aquadaily.app.ui.auth.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferences: PreferencesManager

    private val googleSignInLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Successfully signed in
                Toast.makeText(this, "Welcome, ${account.displayName}!", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
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
            navigateToDashboard()
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

    private fun showForgotPasswordDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Forgot Password")
        builder.setMessage("Enter your email to reset password")

        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Reset") { dialog, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInLauncher.launch(mGoogleSignInClient.signInIntent)
    }

    private fun togglePasswordVisibility() {
        val inputType = binding.etPassword.inputType
        if (inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.alpha = 0.3f
        } else {
            binding.etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.alpha = 1.0f
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun navigateToDashboard() {
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
