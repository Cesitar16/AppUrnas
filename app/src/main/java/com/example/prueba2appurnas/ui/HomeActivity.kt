package com.example.prueba2appurnas.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.databinding.ActivityHomeBinding
import com.example.prueba2appurnas.ui.fragments.AddUrnaFragment
import com.example.prueba2appurnas.ui.fragments.UrnasFragment
import com.example.prueba2appurnas.ui.fragments.ProfileFragment
import com.example.prueba2appurnas.ui.fragments.AdminUsersFragment
import com.example.prueba2appurnas.ui.admin.AdminOrdersFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_urnas -> {
                    loadCleanFragment(UrnasFragment())
                }

                R.id.nav_add_urna -> {
                    loadCleanFragment(AddUrnaFragment())
                }

                R.id.nav_profile -> {
                    loadCleanFragment(ProfileFragment())
                }

                R.id.nav_users -> {
                    loadCleanFragment(AdminUsersFragment())
                }

                R.id.nav_admin_orders -> {
                    // Aquí no limpiamos el backstack para poder volver atrás
                    loadFragment(AdminOrdersFragment(), addToBackStack = true)
                }
            }
            true
        }

        // Primera carga
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_urnas
        }
    }

    /**
     * Limpia el backstack y carga un fragment sin añadirlo atrás.
     */
    private fun loadCleanFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        loadFragment(fragment, addToBackStack = false)
    }

    /**
     * Reemplaza el fragmento, opcionalmente añadiéndolo al backstack.
     */
    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }
}
