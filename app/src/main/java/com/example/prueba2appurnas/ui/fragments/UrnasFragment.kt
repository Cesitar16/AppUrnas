package com.example.prueba2appurnas.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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

    private var _binding: FragmentUrnasBinding? = null
    private val binding get() = _binding!!

    private lateinit var urnaAdapter: UrnaAdapter
    // 1. Añade una variable para guardar la lista completa de urnas
    private var fullUrnasList: List<Urna> = emptyList()

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
        fetchUrnas()

        // 2. Llama a la nueva función para configurar la búsqueda
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewUrnas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewUrnas.isNestedScrollingEnabled = false
    }

    /**
     * Obtiene la lista de urnas de la API.
     */
    private fun fetchUrnas() {
        binding.progressBar.visibility = View.VISIBLE
        val service = RetrofitClient.getUrnaService(requireContext())

        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                if (_binding == null) {
                    Log.w("UrnasFragment", "Binding nulo en onResponse. El fragmento ya no está visible.")
                    return
                }
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val urnas = response.body() ?: emptyList()
                    Log.d("UrnasFragment", "Urnas recibidas: ${urnas.size}")

                    if (urnas.isNotEmpty()) {
                        // 3. Guarda la lista completa en la variable de clase
                        fullUrnasList = urnas

                        // 4. Inicializa el adaptador con la lista completa
                        urnaAdapter = UrnaAdapter(fullUrnasList)
                        binding.recyclerViewUrnas.adapter = urnaAdapter
                        updateDashboard(fullUrnasList)
                    } else {
                        Toast.makeText(requireContext(), "No hay urnas disponibles", Toast.LENGTH_SHORT).show()
                        binding.dashboardContainer.removeAllViews()
                        binding.recyclerViewUrnas.adapter = null
                    }

                } else {
                    Log.e("UrnasFragment", "Error al obtener urnas: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Error ${response.code()}: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                if (_binding == null) {
                    Log.w("UrnasFragment", "Binding nulo en onFailure. El fragmento ya no está visible.")
                    return
                }
                binding.progressBar.visibility = View.GONE
                Log.e("UrnasFragment", "Fallo en la llamada a la API", t)
                Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // 5. Añade la función setupSearch (copiada de tu HomeActivity y adaptada)
    private fun setupSearch() {
        binding.inputBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Asegura que el adaptador esté inicializado
                if (!::urnaAdapter.isInitialized) return

                val query = s.toString().trim().lowercase()

                // Filtra la lista completa (fullUrnasList)
                val filtered = fullUrnasList.filter { urna ->
                    val nombre = urna.name?.lowercase()?.contains(query) ?: false
                    val internalId = urna.internal_id?.lowercase()?.contains(query) ?: false
                    nombre || internalId
                }

                // Actualiza el adaptador con la lista filtrada
                urnaAdapter.updateData(filtered)
            }
        })
    }

    // 6. Reemplaza tu updateDashboard con esta versión (de tu HomeActivity, adaptada)
    private fun updateDashboard(urnas: List<Urna>) {
        // Comprobaciones de seguridad
        if (!isAdded || _binding == null || context == null) return

        binding.dashboardContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        val totalUrnas = urnas.size
        val stockTotal = urnas.sumOf { it.stock ?: 0 }
        val preciosValidos = urnas.mapNotNull { it.price }
        val promedioPrecio = if (preciosValidos.isNotEmpty()) preciosValidos.average() else 0.0

        // 🔸 Contar urnas con stock bajo (5 o menos)
        val urnasBajoStock = urnas.count { (it.stock ?: 0) <= 5 }

        // 🔸 Mostrar las 4 métricas con los iconos correctos
        // Asegúrate de tener estos drawables en tu carpeta res/drawable:
        // ic_inventory, ic_trending_up, ic_sales, ic_warning
        val metrics = listOf(
            Triple(totalUrnas.toString(), getString(R.string.total_urnas), R.drawable.ic_inventory),
            Triple(stockTotal.toString(), getString(R.string.stock_total), R.drawable.ic_trending_up),
            Triple("$${promedioPrecio.toInt()}", getString(R.string.precio_promedio), R.drawable.ic_sales),
            Triple(urnasBajoStock.toString(), getString(R.string.urnas_bajo_stock), R.drawable.ic_warning)
        )

        metrics.forEach { (value, label, iconRes) ->
            try {
                val view = inflater.inflate(R.layout.item_metric_card, binding.dashboardContainer, false)

                view.findViewById<TextView>(R.id.txtMetricValue).text = value
                view.findViewById<TextView>(R.id.txtMetricLabel).text = label

                val icon = view.findViewById<ImageView>(R.id.imgMetricIcon)
                icon.setImageResource(iconRes)
                icon.visibility = View.VISIBLE

                // 7. Adaptación CRÍTICA:
                // Tu fragment_urnas.xml usa un LinearLayout con weightSum.
                // Tu item_metric_card.xml usa layout_width="0dp".
                // Debemos asignar un peso (weight) a cada item.
                val params = LinearLayout.LayoutParams(
                    0, // Width (0dp)
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Height
                    1f // Weight (1f)
                ).apply {
                    // Opcional: añadir márgenes si no están en item_metric_card
                    setMargins(8, 8, 8, 8)
                }

                view.layoutParams = params
                binding.dashboardContainer.addView(view)

            } catch (e: Exception) {
                Log.e("UrnasFragment", "Error al inflar o actualizar item_metric_card", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}