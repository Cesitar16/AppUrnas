package com.example.prueba2appurnas.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.databinding.ActivityProfileBinding
import com.example.prueba2appurnas.ui.fragments.ProfileFragment

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.profileContainer, ProfileFragment())
            }
        }
    }
}
