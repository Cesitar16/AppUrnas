package com.example.prueba2appurnas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.ActivityMainBinding
import com.example.prueba2appurnas.model.LoginRequest
import com.example.prueba2appurnas.ui.client.ClientHomeActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // 🔥 Si ya hay token → verificar rol directo
        if (tokenManager.isLoggedIn()) {
            verificarRolYRedirigir()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.txtCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthService(this@MainActivity)
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val auth = response.body()
                    if (auth != null) {
                        // Guardar token
                        tokenManager.saveToken(auth.authToken)

                        // Después de login → verificar usuario y rol
                        verificarRolYRedirigir()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun verificarRolYRedirigir() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthenticatedAuthService(this@MainActivity)
                val response = api.getUser()

                if (!response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "No se pudo verificar el usuario", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = response.body()
                if (user == null) {
                    Toast.makeText(this@MainActivity, "Usuario inválido", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Guardar email del usuario
                tokenManager.saveUserEmail(user.email)

                // Revisar rol
                val rol = user.rol.lowercase()

                when (rol) {
                    "admin" ->
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))

                    "client", "cliente", "user" ->
                        startActivity(Intent(this@MainActivity, ClientHomeActivity::class.java))

                    else ->
                        Toast.makeText(this@MainActivity, "Rol desconocido: $rol", Toast.LENGTH_SHORT).show()
                }

                finish()

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
