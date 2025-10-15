package com.example.prueba2appurnas.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.model.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : BottomSheetDialogFragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var contentGroup: View
    private lateinit var errorGroup: View
    private lateinit var txtError: TextView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtRole: TextView
    private lateinit var btnRetry: Button
    private lateinit var btnClose: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progressProfile)
        contentGroup = view.findViewById(R.id.profileContent)
        errorGroup = view.findViewById(R.id.profileErrorGroup)
        txtError = view.findViewById(R.id.txtProfileError)
        txtName = view.findViewById(R.id.txtProfileNameValue)
        txtEmail = view.findViewById(R.id.txtProfileEmailValue)
        txtRole = view.findViewById(R.id.txtProfileRoleValue)
        btnRetry = view.findViewById(R.id.btnProfileRetry)
        btnClose = view.findViewById(R.id.btnProfileClose)

        btnRetry.setOnClickListener { fetchUser() }
        btnClose.setOnClickListener { dismiss() }

        fetchUser()
    }

    private fun fetchUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            showLoading(true)
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.getAuthService(requireContext()).getUser()
                }
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        showUser(user)
                    } else {
                        showError(getString(R.string.profile_error_empty))
                    }
                } else {
                    showError(getString(R.string.profile_error_status, response.code()))
                }
            } catch (exception: Exception) {
                showError(exception.localizedMessage ?: getString(R.string.profile_error_generic))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showUser(user: User) {
        contentGroup.isVisible = true
        errorGroup.isVisible = false

        txtName.text = user.name.ifBlank { getString(R.string.profile_unknown) }
        txtEmail.text = user.email.ifBlank { getString(R.string.profile_unknown) }
        txtRole.text = user.role.ifBlank { getString(R.string.profile_unknown) }
    }

    private fun showError(message: String) {
        contentGroup.isVisible = false
        errorGroup.isVisible = true
        txtError.text = message
    }

    private fun showLoading(show: Boolean) {
        progressBar.isVisible = show
        if (show) {
            contentGroup.isVisible = false
            errorGroup.isVisible = false
        }
    }

    companion object {
        const val TAG = "ProfileFragment"
    }
}
