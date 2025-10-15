package com.miapp.xanostorekotlin.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.ApiConfig
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.api.TokenManager
import com.miapp.xanostorekotlin.databinding.ActivityMainBinding
import com.miapp.xanostorekotlin.model.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tokenManager by lazy { TokenManager(this) }
    private val authService by lazy { RetrofitClient.createAuthService(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (tokenManager.isLoggedIn() && !tokenManager.hasExpired(ApiConfig.tokenTtlSec)) {
            navigateToHome()
            return
        }

        binding.emailInput.doAfterTextChanged { updateButtonState() }
        binding.passwordInput.doAfterTextChanged { updateButtonState() }

        binding.loginButton.setOnClickListener { attemptLogin() }
    }

    private fun updateButtonState() {
        val hasEmail = binding.emailInput.text?.isNotBlank() == true
        val passwordLength = binding.passwordInput.text?.length ?: 0
        binding.loginButton.isEnabled = hasEmail && passwordLength >= 4
    }

    private fun attemptLogin() {
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString()?.trim().orEmpty()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Por favor ingresa tus credenciales", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = authService.login(LoginRequest(email, password))
                tokenManager.saveAuth(response)
                Toast.makeText(this@MainActivity, "Bienvenido ${response.user?.name ?: ""}", Toast.LENGTH_SHORT).show()
                navigateToHome()
            } catch (ex: Exception) {
                handleError(ex)
            } finally {
                binding.progressBar.visibility = View.GONE
                updateButtonState()
            }
        }
    }

    private fun handleError(ex: Exception) {
        val message = when (ex) {
            is HttpException -> when (ex.code()) {
                400, 401 -> "Credenciales inválidas"
                else -> "Error ${ex.code()} al iniciar sesión"
            }
            else -> ex.localizedMessage ?: "Error desconocido"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
