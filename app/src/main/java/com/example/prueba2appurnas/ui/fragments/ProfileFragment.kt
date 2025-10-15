package com.example.prueba2appurnas.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.databinding.FragmentProfileBinding
import com.example.prueba2appurnas.ui.MainActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
        val tokenManager = TokenManager(requireContext())
        val user = tokenManager.getUser()

        if (user != null) {
            binding.txtUserName.text = user.name
            binding.txtUserEmail.text = user.email
            binding.txtUserRole.text = user.role
        } else {
            binding.txtUserName.text = getString(R.string.profile_unknown_user)
            binding.txtUserEmail.text = getString(R.string.profile_unknown_email)
            binding.txtUserRole.text = getString(R.string.profile_unknown_role)
        }

        binding.btnLogout.setOnClickListener {
            tokenManager.clearToken()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
