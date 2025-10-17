package com.example.prueba2appurnas.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.databinding.ActivityHomeBinding
import com.example.prueba2appurnas.ui.fragments.AddUrnaFragment // Nombres actualizados
import com.example.prueba2appurnas.ui.fragments.UrnasFragment   // Nombres actualizados
import com.example.prueba2appurnas.ui.fragments.ProfileFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener para la navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_urnas -> selectedFragment = UrnasFragment() // Carga el fragmento de lista
                R.id.nav_add_urna -> selectedFragment = AddUrnaFragment() // Carga el fragmento de añadir
                R.id.nav_profile -> selectedFragment = ProfileFragment() // Carga el fragmento de perfil
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment) // Llama a la función para cargar
            }
            true // Importante: indica que el evento se manejó
        }

        // Carga el fragmento inicial la primera vez que se abre la actividad
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_urnas
            // Nota: El listener se disparará automáticamente al establecer el item seleccionado,
            // cargando así el UrnasFragment inicial. No es necesario llamarlo dos veces.
        }
    }

    /**
     * Reemplaza el contenido del FrameLayout con el fragmento especificado.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Usa el ID del FrameLayout
            .commit() // Ejecuta la transacción
    }

    // --- NO HAY LÓGICA DE fetchUrnas, updateDashboard, NI setupRecyclerView AQUÍ ---
}