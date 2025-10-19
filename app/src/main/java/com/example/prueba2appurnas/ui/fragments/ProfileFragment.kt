// En: src/main/java/com/example/prueba2appurnas/ui/fragments/ProfileFragment.kt

package com.example.prueba2appurnas.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // Asegúrate de importar Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Importa lifecycleScope
import com.example.prueba2appurnas.api.AuthService // Importa AuthService
import com.example.prueba2appurnas.api.RetrofitClient // Importa RetrofitClient
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.FragmentProfileBinding
import com.example.prueba2appurnas.ui.MainActivity
import kotlinx.coroutines.launch // Importa launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager
    private lateinit var authService: AuthService // Añade la instancia del servicio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context == null) {
            Log.e("ProfileFragment", "Contexto nulo en onViewCreated")
            return
        }
        tokenManager = TokenManager(requireContext())

        // *** CORRECCIÓN AQUÍ ***
        // Usa la nueva función para obtener el servicio autenticado
        authService = RetrofitClient.getAuthenticatedUserService(requireContext())
        // *** FIN CORRECCIÓN ***

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        fetchAndDisplayUserInfo()
    }

    /**
     * Llama a la API /auth/me y muestra los datos del usuario.
     */
    private fun fetchAndDisplayUserInfo() {
        // Muestra un estado de carga inicial
        binding.tvUserName.text = "Cargando..."
        binding.tvUserEmail.text = "Cargando..."
        binding.tvUserRole.text = "Cargando..."
        // Opcional: Mostrar un ProgressBar
        // binding.progressBar.visibility = View.VISIBLE

        // Verifica si hay token antes de hacer la llamada
        if (!tokenManager.isLoggedIn()) {
            Log.w("ProfileFragment", "No hay token, no se puede llamar a /auth/me. Redirigiendo a Login.")
            Toast.makeText(context, "Sesión expirada.", Toast.LENGTH_SHORT).show()
            logoutUser() // Redirige a login si no hay token
            return
        }

        lifecycleScope.launch {
            try {
                val response = authService.getUser() // Llama a la función suspendida

                // Opcional: Ocultar ProgressBar
                // if (_binding != null) binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        Log.d("ProfileFragment", "Datos recibidos de /auth/me: Name=${user.user}, Email=${user.email}, Role=${user.rol}")
                        // Actualiza la UI si el binding todavía existe
                        _binding?.let {
                            it.tvUserName.text = user.user ?: "Nombre no disponible"
                            it.tvUserEmail.text = user.email ?: "Email no disponible"
                            it.tvUserRole.text = user.rol ?: "Rol no especificado"
                        }
                        // Guarda los datos en SharedPreferences (Opcional, si quieres caché local)
                        // tokenManager.saveAuthData(tokenManager.getToken()!!, user.name, user.email, user.role) // Necesitarías re-añadir esta lógica a TokenManager si lo haces
                    } else {
                        Log.w("ProfileFragment", "/auth/me exitoso pero cuerpo de respuesta nulo.")
                        if (_binding != null) displayErrorState("Respuesta inesperada del servidor")
                    }
                } else {
                    // Error de API (ej: token inválido, 401 Unauthorized)
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("ProfileFragment", "Error en /auth/me (${response.code()}): $errorBody")
                    if (_binding != null) displayErrorState("Error ${response.code()}")
                    // Si el error es 401, podría ser bueno redirigir a Login
                    if (response.code() == 401) {
                        Toast.makeText(context, "Sesión inválida.", Toast.LENGTH_SHORT).show()
                        logoutUser()
                    }
                }
            } catch (e: Exception) {
                // Error de red u otro inesperado
                Log.e("ProfileFragment", "Excepción durante /auth/me: ${e.message}", e)
                // Opcional: Ocultar ProgressBar
                // if (_binding != null) binding.progressBar.visibility = View.GONE
                if (_binding != null) displayErrorState("Error de conexión")
            }
        }
    }

    /** Helper para mostrar estado de error en la UI */
    private fun displayErrorState(message: String) {
        binding.tvUserName.text = message
        binding.tvUserEmail.text = "No se pudo cargar"
        binding.tvUserRole.text = "No se pudo cargar"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Borra el token y navega a MainActivity (Login). (Sin cambios)
     */
    private fun logoutUser() {
        Log.d("ProfileFragment", "Cerrando sesión...")
        if (!::tokenManager.isInitialized) {
            Log.e("ProfileFragment", "TokenManager no inicializado al intentar logout.")
            if (context != null) tokenManager = TokenManager(requireContext())
            else return
        }
        tokenManager.clearToken()

        if (activity == null || !isAdded) {
            Log.w("ProfileFragment", "Actividad nula o Fragment no añadido al intentar navegar a Login.")
            return
        }
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}