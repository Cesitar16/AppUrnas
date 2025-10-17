package com.example.prueba2appurnas.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.FragmentProfileBinding // Binding generado
import com.example.prueba2appurnas.ui.MainActivity // Tu pantalla de Login

class ProfileFragment : Fragment() {

    // View Binding seguro
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar TokenManager usando requireContext()
        tokenManager = TokenManager(requireContext())

        // Configurar el botón de logout
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // --- TODO: Cargar y mostrar info del usuario ---
        // Aquí podrías, por ejemplo, obtener el email guardado en SharedPreferences
        // o hacer una llamada a un endpoint como /auth/me si existe en tu API.
        // val userEmail = // ... obtener email
        // binding.tvUserInfo.text = "Usuario: $userEmail"
        binding.tvUserInfo.text = "¡Bienvenido!" // Mensaje temporal

    }

    /**
     * Borra el token de autenticación y navega a la pantalla de Login (MainActivity),
     * finalizando la actividad actual (HomeActivity).
     */
    private fun logoutUser() {
        Log.d("ProfileFragment", "Cerrando sesión...")
        // Borrar el token guardado
        tokenManager.clearToken()

        // Crear intent para ir a MainActivity (Login)
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            // Flags para limpiar el stack de actividades:
            // - FLAG_ACTIVITY_NEW_TASK: Inicia MainActivity en una nueva tarea.
            // - FLAG_ACTIVITY_CLEAR_TASK: Elimina todas las actividades existentes de la tarea.
            // Resultado: Al presionar "Atrás" en Login, la app se cierra en lugar de volver a Home.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Finalizar la actividad actual (HomeActivity) para que no quede en segundo plano
        requireActivity().finish()
    }

    /**
     * Limpia el binding al destruir la vista del fragmento.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ¡Fundamental para evitar memory leaks!
    }
}