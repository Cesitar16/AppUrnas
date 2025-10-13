package com.example.prueba2appurnas.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prueba2appurnas.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val username = binding.inputUsername.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

            // Validaciones
            when {
                username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showToast("Por favor completa todos los campos")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showToast("Ingresa un correo válido (con @ y dominio)")
                }
                password != confirmPassword -> {
                    showToast("Las contraseñas no coinciden")
                }
                else -> {
                    registerUser(username, email, password)
                }
            }
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        val url = "https://backend-descansos-del-recuerdo-spa.onrender.com/auth/register"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                }

                // JSON con rol por defecto "cliente"
                val body = JSONObject().apply {
                    put("username", username)
                    put("email", email)
                    put("password", password)
                    put("rol", "cliente")
                }

                OutputStreamWriter(connection.outputStream).use {
                    it.write(body.toString())
                    it.flush()
                }

                val responseCode = connection.responseCode
                withContext(Dispatchers.Main) {
                    if (responseCode in 200..299) {
                        showToast("Registro exitoso")
                        // Redirigir al login
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    } else {
                        showToast("Error al registrar (${connection.responseMessage})")
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error de conexión: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
