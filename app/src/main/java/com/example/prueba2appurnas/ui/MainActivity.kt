package com.example.prueba2appurnas.ui
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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

        // 1. Configurar listeners SIEMPRE (antes de cualquier chequeo)
        setupListeners()

        // 2. Verificar sesión automática
        if (tokenManager.isLoggedIn()) {
            // Deshabilitamos la UI visualmente mientras carga, pero no bloqueamos la lógica
            setLoadingState(true)
            verificarRolYRedirigir(isAutoLogin = true)
        }
    }

    private fun setupListeners() {
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
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthService(this@MainActivity)
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    val auth = response.body()
                    if (auth != null) {
                        tokenManager.saveToken(auth.authToken)
                        // Verificar rol (Login manual)
                        verificarRolYRedirigir(isAutoLogin = false)
                    } else {
                        setLoadingState(false)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    setLoadingState(false)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                setLoadingState(false)
            }
        }
    }

    // Añadí el parámetro isAutoLogin para manejar mejor los errores
    private fun verificarRolYRedirigir(isAutoLogin: Boolean) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getAuthenticatedAuthService(this@MainActivity)
                val response = api.getUser()

                if (!response.isSuccessful) {
                    // 🔥 Si falla (ej. Token Expirado 401), limpiamos y dejamos loguear de nuevo
                    if (isAutoLogin) {
                        Toast.makeText(this@MainActivity, "Sesión expirada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error al verificar usuario", Toast.LENGTH_SHORT).show()
                    }
                    tokenManager.clear() // Importante: borrar el token malo
                    setLoadingState(false)
                    return@launch
                }

                val user = response.body()
                if (user == null) {
                    Toast.makeText(this@MainActivity, "Datos de usuario vacíos", Toast.LENGTH_SHORT).show()
                    setLoadingState(false)
                    return@launch
                }

                tokenManager.saveUserId(user.id)
                tokenManager.saveUserEmail(user.email)

                val rol = user.rol.lowercase()
                val intent = when (rol) {
                    "admin" -> Intent(this@MainActivity, HomeActivity::class.java)
                    "client", "cliente", "user" -> Intent(this@MainActivity, ClientHomeActivity::class.java)
                    else -> null
                }

                if (intent != null) {
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Rol no permitido: $rol", Toast.LENGTH_SHORT).show()
                    tokenManager.clear()
                    setLoadingState(false)
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                // Si hay error de red en auto-login, permitimos intentar manual
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.inputEmail.isEnabled = !isLoading
        binding.inputPassword.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Cargando..." else "Iniciar Sesión"
    }
}