package com.example.prueba2appurnas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.ActivityRegisterBinding
import com.example.prueba2appurnas.model.SignupRequest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        binding.btnRegister.setOnClickListener {
            val name = binding.inputUsername.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, password)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthService(this@RegisterActivity)
                val response = api.signup(SignupRequest(name, email, password))

                if (response.isSuccessful) {
                    response.body()?.let {
                        tokenManager.saveToken(it.authToken)
                        tokenManager.saveUser(it.user)
                        startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
