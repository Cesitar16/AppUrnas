package com.example.prueba2appurnas.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.databinding.FragmentAdminUsersBinding
import com.example.prueba2appurnas.model.User
import com.example.prueba2appurnas.ui.admin.AdminUserAdapter
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadUsers()
    }

    private fun setupRecyclerView() {
        // Inicializamos el adapter con los DOS callbacks: cambio de estado y borrar
        adapter = AdminUserAdapter(
            users = emptyList(),
            onStatusChange = { user, isActive ->
                toggleUserStatus(user, isActive)
            },
            onDeleteClick = { user ->
                confirmDeleteUser(user)
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        val service = RetrofitClient.getUserService(requireContext())

        lifecycleScope.launch {
            try {
                val response = service.getAllUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    adapter.updateList(users)
                } else {
                    Toast.makeText(context, "Error cargando usuarios: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // --- Lógica para Activar/Desactivar ---
    // Reemplaza SOLO la función toggleUserStatus con esta versión:
    private fun toggleUserStatus(user: User, isActive: Boolean) {
        val userService = RetrofitClient.getUserService(requireContext())

        // CORRECCIÓN: Agregamos "role" porque Xano lo exige como obligatorio.
        val updateData = mapOf(
            "Activo" to isActive,
            "name" to (user.name ?: ""),
            "email" to (user.email ?: ""),
            "role" to user.rol // Enviamos el rol actual del usuario
        )

        lifecycleScope.launch {
            try {
                // La llamada ahora envía: Activo, name, email y role
                val response = userService.adminUpdateUser(user.id, updateData)

                if (response.isSuccessful) {
                    val estado = if (isActive) "activado" else "desactivado"
                    Toast.makeText(context, "Usuario $estado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                    loadUsers() // Revertir si falla
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                loadUsers() // Revertir si falla
            }
        }
    }

    // --- Lógica de Eliminación (Sin cambios mayores) ---
    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ -> deleteUser(user.id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(userId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        val userService = RetrofitClient.getUserService(requireContext())

        lifecycleScope.launch {
            try {
                val response = userService.deleteUser(userId)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    loadUsers()
                } else {
                    Toast.makeText(context, "Error al eliminar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}