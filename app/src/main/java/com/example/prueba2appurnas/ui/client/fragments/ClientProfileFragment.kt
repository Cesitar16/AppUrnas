package com.example.prueba2appurnas.ui.client.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.FragmentClientProfileBinding
import com.example.prueba2appurnas.ui.MainActivity
import kotlinx.coroutines.launch

class ClientProfileFragment : Fragment() {

    private var _binding: FragmentClientProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManager

    // Variables para almacenar los datos actuales del usuario
    private var currentUserId: Int = -1
    private var currentUserRole: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProfileBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos iniciales
        loadUserProfile()

        // Botón Guardar
        binding.btnSaveChangesClient.setOnClickListener {
            saveChanges()
        }

        // Botón Cerrar Sesión
        binding.btnLogoutClient.setOnClickListener {
            tokenManager.clear()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun loadUserProfile() {
        setLoading(true)
        val authService = RetrofitClient.getAuthenticatedAuthService(requireContext())

        lifecycleScope.launch {
            try {
                val response = authService.getUser()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    // Guardamos datos críticos
                    currentUserId = user.id
                    currentUserRole = user.rol

                    // 1. Mostrar nombre en el TextView superior
                    binding.tvDisplayName.text = user.name

                    // 2. Poblar campos editables
                    binding.etUserNameClient.setText(user.name)
                    binding.etUserEmailClient.setText(user.email)
                } else {
                    Toast.makeText(requireContext(), "Error cargando perfil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun saveChanges() {
        val newName = binding.etUserNameClient.text.toString().trim()
        val emailInput = binding.etUserEmailClient.text.toString().trim()

        if (newName.isEmpty()) {
            binding.etUserNameClient.error = "El nombre no puede estar vacío"
            return
        }
        if (emailInput.isEmpty()) {
            binding.etUserEmailClient.error = "El correo no puede estar vacío"
            return
        }
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val userService = RetrofitClient.getUserService(requireContext())

        // CORRECCIÓN FINAL:
        // Lectura (GET) -> "user"
        // Escritura (PATCH) -> requiere "name", "email", "role"
        val updateData = mapOf(
            "name" to newName,
            "email" to emailInput,
            "role" to currentUserRole
        )

        lifecycleScope.launch {
            try {
                val response = userService.updateUser(currentUserId, updateData)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "¡Perfil actualizado con éxito!", Toast.LENGTH_LONG).show()

                    // Actualizamos visualmente el nombre en la cabecera
                    binding.tvDisplayName.text = newName

                    // Opcional: Actualizar el email en SharedPreferences si cambió
                    tokenManager.saveUserEmail(emailInput)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(requireContext(), "Error al actualizar: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (_binding == null) return
        binding.progressBarClient.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveChangesClient.isEnabled = !isLoading
        binding.etUserNameClient.isEnabled = !isLoading
        binding.etUserEmailClient.isEnabled = !isLoading
        binding.btnLogoutClient.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}