package com.example.prueba2appurnas.ui.fragments

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
import com.example.prueba2appurnas.databinding.FragmentProfileBinding
import com.example.prueba2appurnas.ui.MainActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var tokenManager: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfileData()

        binding.btnLogout.setOnClickListener {
            tokenManager.clear()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun loadProfileData() {
        val api = RetrofitClient.getAuthenticatedAuthService(requireContext())

        lifecycleScope.launch {
            try {
                val response = api.getUser() // suspend fun → correcto

                if (!response.isSuccessful) {
                    Toast.makeText(requireContext(), "No se pudo obtener los datos", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val user = response.body() ?: return@launch

                // IDs correctos del XML
                binding.tvUserName.text = user.name ?: "Sin nombre"
                binding.tvUserEmail.text = user.email ?: "Sin correo"
                binding.tvUserRole.text = user.rol ?: "Sin rol"

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
