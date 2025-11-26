package com.example.prueba2appurnas.ui.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.ui.client.fragments.ClientCatalogFragment
import com.example.prueba2appurnas.ui.client.fragments.ClientCartFragment
import com.example.prueba2appurnas.ui.client.fragments.ClientProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ClientHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavClient)

        // Fragment inicial
        loadFragment(ClientCatalogFragment())

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.menu_client_catalog -> ClientCatalogFragment()
                R.id.menu_client_cart -> ClientCartFragment()
                R.id.menu_client_profile -> ClientProfileFragment()
                else -> null
            }

            fragment?.let { loadFragment(it) }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.clientFragmentContainer, fragment)
            .commit()
    }
}
