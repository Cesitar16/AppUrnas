package com.example.prueba2appurnas.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.FragmentProfileBinding // Asegúrate que usa el binding correcto
import com.example.prueba2appurnas.ui.MainActivity // Tu pantalla de Login

class ProfileFragment : Fragment() {

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

        // Verificar contexto antes de inicializar TokenManager
        if (context == null) {
            Log.e("ProfileFragment", "Contexto nulo en onViewCreated")
            return // Salir temprano si no hay contexto
        }
        tokenManager = TokenManager(requireContext())

        // Configurar el botón de logout (sin cambios)
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        // --- CARGAR Y MOSTRAR INFO DEL USUARIO ---
        loadAndDisplayUserInfo()
    }

    /**
     * Obtiene los datos del usuario desde TokenManager y los muestra en la UI.
     */
    private fun loadAndDisplayUserInfo() {
        // Asegurarse que tokenManager está inicializado
        if (!::tokenManager.isInitialized) {
            Log.e("ProfileFragment", "TokenManager no inicializado.")
            binding.tvUserName.text = "Error al cargar"
            binding.tvUserEmail.text = "Error al cargar"
            binding.tvUserRole.text = "Error al cargar"
            return
        }

        val name = tokenManager.getUserName()
        val email = tokenManager.getUserEmail()
        val role = tokenManager.getUserRole()

        Log.d("ProfileFragment", "Datos recuperados: Name=$name, Email=$email, Role=$role")

        binding.tvUserName.text = name ?: "Nombre no disponible"
        binding.tvUserEmail.text = email ?: "Email no disponible"
        binding.tvUserRole.text = role ?: "Rol no especificado"

        // Ya no necesitamos el tvUserInfo original
        // binding.tvUserInfo.text = "Usuario: ${email ?: "Desconocido"} \nRol: ${role ?: "N/A"}"
    }


    /**
     * Borra el token y navega a MainActivity (Login).
     */
    private fun logoutUser() {
        Log.d("ProfileFragment", "Cerrando sesión...")
        // Verificar si tokenManager está inicializado
        if (!::tokenManager.isInitialized) {
            Log.e("ProfileFragment", "TokenManager no inicializado al intentar logout.")
            // Opcional: intentar inicializarlo de nuevo si el contexto está disponible
            if (context != null) tokenManager = TokenManager(requireContext())
            else return // No se puede hacer logout sin TokenManager
        }

        tokenManager.clearToken() // Borrar todos los datos guardados

        // Verificar si la actividad aún existe antes de crear Intent y finalizar
        if (activity == null || !isAdded) {
            Log.w("ProfileFragment", "Actividad nula o Fragment no añadido al intentar navegar a Login.")
            return
        }

        // Crear intent para ir a MainActivity (Login)
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Finalizar la actividad actual (HomeActivity)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}