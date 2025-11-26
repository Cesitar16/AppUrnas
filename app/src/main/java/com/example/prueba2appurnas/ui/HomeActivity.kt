package com.example.prueba2appurnas.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.databinding.ActivityHomeBinding
import com.example.prueba2appurnas.ui.fragments.AddUrnaFragment // Nombres actualizados
import com.example.prueba2appurnas.ui.fragments.UrnasFragment   // Nombres actualizados
import com.example.prueba2appurnas.ui.fragments.ProfileFragment
import com.example.prueba2appurnas.ui.fragments.AdminUsersFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_urnas -> selectedFragment = UrnasFragment()
                R.id.nav_add_urna -> selectedFragment = AddUrnaFragment()
                R.id.nav_profile -> selectedFragment = ProfileFragment()
                R.id.nav_users -> selectedFragment = AdminUsersFragment()
                R.id.nav_profile -> selectedFragment = ProfileFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                loadFragment(selectedFragment, false)
            }
            true
        }

        // Carga inicial sin añadir a la pila
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_urnas // Dispara el listener y carga UrnasFragment
        }
    }

    /**
     * Reemplaza el contenido del FrameLayout con el fragmento especificado.
     */
    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) { // Modificado para aceptar parámetro
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null) // Nombre opcional para la transacción
        }
        // No necesitamos else, si no se añade, simplemente se reemplaza

        transaction.commit()
    }

    // --- NO HAY LÓGICA DE fetchUrnas, updateDashboard, NI setupRecyclerView AQUÍ ---
}