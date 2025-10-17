package com.example.prueba2appurnas.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.databinding.FragmentUrnasBinding // Binding generado
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.ui.UrnaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UrnasFragment : Fragment() {

    // View Binding seguro para fragmentos
    private var _binding: FragmentUrnasBinding? = null
    private val binding get() = _binding!!

    private lateinit var urnaAdapter: UrnaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUrnasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchUrnas() // Llama a cargar los datos cuando la vista está lista

        // Aquí podrías añadir lógica para el EditText 'inputBuscar' si quieres implementar búsqueda
        // binding.inputBuscar.addTextChangedListener { ... }
    }

    /**
     * Configura el RecyclerView (LayoutManager).
     */
    private fun setupRecyclerView() {
        binding.recyclerViewUrnas.layoutManager = LinearLayoutManager(requireContext())
        // Deshabilita el scroll interno del RecyclerView porque ya está dentro de un NestedScrollView
        binding.recyclerViewUrnas.isNestedScrollingEnabled = false
    }

    /**
     * Obtiene la lista de urnas de la API.
     */
    private fun fetchUrnas() {
        binding.progressBar.visibility = View.VISIBLE // Muestra el ProgressBar
        val service = RetrofitClient.getUrnaService(requireContext()) // Obtiene el servicio API

        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                // Comprobación esencial: ¿El fragmento sigue visible?
                if (_binding == null) {
                    Log.w("UrnasFragment", "Binding nulo en onResponse. El fragmento ya no está visible.")
                    return
                }
                binding.progressBar.visibility = View.GONE // Oculta el ProgressBar

                if (response.isSuccessful) {
                    val urnas = response.body() ?: emptyList() // Obtiene la lista o una lista vacía
                    Log.d("UrnasFragment", "Urnas recibidas: ${urnas.size}")

                    if (urnas.isNotEmpty()) {
                        // Crea el adaptador con los datos y lo asigna al RecyclerView
                        urnaAdapter = UrnaAdapter(urnas) // Tu adaptador existente
                        binding.recyclerViewUrnas.adapter = urnaAdapter
                        updateDashboard(urnas) // Actualiza las tarjetas del dashboard
                    } else {
                        // Si no hay urnas, muestra un mensaje y limpia el dashboard
                        Toast.makeText(requireContext(), "No hay urnas disponibles", Toast.LENGTH_SHORT).show()
                        binding.dashboardContainer.removeAllViews()
                        // Podrías mostrar un TextView indicando que no hay datos en lugar del RecyclerView
                        binding.recyclerViewUrnas.adapter = null // Limpia el adaptador
                    }

                } else {
                    // Error en la respuesta (ej: 401 No autorizado, 404 No encontrado)
                    Log.e("UrnasFragment", "Error al obtener urnas: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Error ${response.code()}: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                // Comprobación esencial
                if (_binding == null) {
                    Log.w("UrnasFragment", "Binding nulo en onFailure. El fragmento ya no está visible.")
                    return
                }
                binding.progressBar.visibility = View.GONE // Oculta el ProgressBar

                // Error de red o al procesar la respuesta
                Log.e("UrnasFragment", "Fallo en la llamada a la API", t)
                Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Actualiza las tarjetas del dashboard con las métricas calculadas.
     * (Lógica movida de la antigua HomeActivity)
     */
    private fun updateDashboard(urnas: List<Urna>) {
        // Comprobaciones de seguridad
        if (!isAdded || _binding == null || context == null) return

        binding.dashboardContainer.removeAllViews() // Limpia las tarjetas anteriores
        val inflater = LayoutInflater.from(requireContext()) // Usa el contexto del fragmento

        // Cálculos (igual que antes, con protecciones)
        val totalUrnas = urnas.size
        val stockTotal = urnas.sumOf { it.stock ?: 0 }
        val preciosValidos = urnas.mapNotNull { it.price }
        val promedioPrecio = if (preciosValidos.isNotEmpty()) preciosValidos.average() else 0.0
        val disponibles = urnas.count { it.available == true }

        // Define las métricas a mostrar
        val metrics = listOf(
            Pair(totalUrnas.toString(), "Total Urnas"), // Etiquetas descriptivas
            Pair(stockTotal.toString(), "Stock Total"),
            Pair("$${"%.0f".format(promedioPrecio)}", "Precio Prom."), // Formato sin decimales
            Pair(disponibles.toString(), "Disponibles")
        )

        // Crea e infla cada tarjeta
        metrics.forEach { (value, label) ->
            try {
                val view = inflater.inflate(R.layout.item_metric_card, binding.dashboardContainer, false)
                view.findViewById<TextView>(R.id.txtMetricValue)?.text = value
                view.findViewById<TextView>(R.id.txtMetricLabel)?.text = label
                binding.dashboardContainer.addView(view)
            } catch (e: Exception) {
                Log.e("UrnasFragment", "Error al inflar o actualizar item_metric_card", e)
            }
        }
    }

    /**
     * Limpia la referencia al binding cuando la vista del fragmento se destruye
     * para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // ¡Muy importante!
    }
}