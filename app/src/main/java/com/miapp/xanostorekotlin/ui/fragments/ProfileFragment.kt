package com.miapp.xanostorekotlin.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.miapp.xanostorekotlin.api.RetrofitClient
import com.miapp.xanostorekotlin.api.TokenManager
import com.miapp.xanostorekotlin.databinding.FragmentProfileBinding
import com.miapp.xanostorekotlin.model.User
import com.miapp.xanostorekotlin.ui.MainActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val tokenManager by lazy { TokenManager(requireContext()) }
    private val authService by lazy { RetrofitClient.createAuthService(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.retryButton.setOnClickListener { loadProfile() }
        binding.logoutButton.setOnClickListener {
            tokenManager.clear()
            startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
        loadProfile()
    }

    private fun loadProfile() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentGroup.visibility = View.GONE
        binding.errorGroup.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = authService.getMe()
                showUser(user)
            } catch (ex: Exception) {
                binding.errorGroup.visibility = View.VISIBLE
                binding.errorMessage.text = when (ex) {
                    is HttpException -> "Error ${ex.code()} al obtener perfil"
                    else -> ex.localizedMessage ?: "Error desconocido"
                }
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showUser(user: User) {
        binding.contentGroup.visibility = View.VISIBLE
        binding.userName.text = user.name
        binding.userEmail.text = user.email
        binding.userCreatedAt.text = user.createdAt.orEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProfileFragment"
        fun newInstance(): Pair<String, ProfileFragment> = TAG to ProfileFragment()
    }
}
