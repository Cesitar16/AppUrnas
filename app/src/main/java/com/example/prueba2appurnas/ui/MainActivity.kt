package com.example.prueba2appurnas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.ActivityMainBinding
import com.example.prueba2appurnas.model.LoginRequest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // Acción de iniciar sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Ir a registro
        binding.txtCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthService(this@MainActivity)
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    response.body()?.let {
                        tokenManager.saveToken(it.authToken)
                        tokenManager.saveUser(it.user)
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
