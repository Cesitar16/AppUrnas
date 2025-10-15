package com.miapp.xanostorekotlin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.api.ApiConfig
import com.miapp.xanostorekotlin.api.TokenManager
import com.miapp.xanostorekotlin.databinding.ActivityHomeBinding
import com.miapp.xanostorekotlin.ui.MainActivity
import com.miapp.xanostorekotlin.ui.fragments.AddProductFragment
import com.miapp.xanostorekotlin.ui.fragments.ProductsFragment
import com.miapp.xanostorekotlin.ui.fragments.ProfileFragment

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val tokenManager by lazy { TokenManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!tokenManager.isLoggedIn() || tokenManager.hasExpired(ApiConfig.tokenTtlSec)) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setupBottomNavigation(binding.bottomNavigation)
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_products
            showFragment(ProductsFragment.newInstance())
        }
    }

    private fun setupBottomNavigation(bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> showFragment(ProductsFragment.newInstance())
                R.id.nav_add -> showFragment(AddProductFragment.newInstance())
                R.id.nav_profile -> showFragment(ProfileFragment.newInstance())
                else -> false
            }
        }
    }

    private fun showFragment(fragmentTagPair: Pair<String, Fragment>): Boolean {
        val (tag, fragment) = fragmentTagPair
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, fragment, tag)
        }
        return true
    }
}
