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

        // --- VERIFICAR SI YA HAY SESIÓN ---
        // Es buena práctica verificar si ya existe un token válido al iniciar MainActivity.
        // Si existe, ir directamente a HomeActivity.
        if (tokenManager.isLoggedIn()) {
            Log.d("MainActivity", "Usuario ya logueado, redirigiendo a Home.")
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            finish() // Cierra MainActivity para que no quede en la pila
            return // Salir de onCreate temprano
        }
        // --- FIN VERIFICACIÓN ---


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

    @OptIn(UnstableApi::class) // Mantén esto si lo necesitas
    private fun loginUser(email: String, password: String) {
        // Mostrar ProgressBar y deshabilitar botón (opcional pero recomendado)
        // binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            try {
                // AuthService sigue siendo necesario para el login
                val api = RetrofitClient.getAuthService(this@MainActivity)
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        // *** CORRECCIÓN PRINCIPAL ***
                        // Solo guardamos el token
                        Log.d("MainActivity", "Login exitoso. Guardando token: ${authResponse.authToken}")
                        tokenManager.saveToken(authResponse.authToken) // Usa saveToken en lugar de saveAuthData

                        // Ya no intentamos guardar name, email, role aquí

                        // Ir a Home (sin cambios)
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                        finish() // Cerrar MainActivity después del login exitoso
                    } ?: run {
                        Log.w("MainActivity", "Login exitoso pero cuerpo de respuesta nulo.")
                        Toast.makeText(this@MainActivity, "Respuesta inesperada del servidor", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("MainActivity", "Error en login (${response.code()}): $errorBody")
                    Toast.makeText(this@MainActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Excepción durante login: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Ocultar ProgressBar y habilitar botón (sin cambios)
                // binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
            }
        }
    }
}
