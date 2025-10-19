package com.example.prueba2appurnas.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.databinding.FragmentUrnasBinding
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.ui.UrnaAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UrnasFragment : Fragment() {

    private var _binding: FragmentUrnasBinding? = null
    private val binding get() = _binding!!

    private lateinit var urnaAdapter: UrnaAdapter
    private var urnasOriginales: List<Urna> = emptyList()

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
        setupSearch()
        fetchUrnas()
    }

    /** Configura el RecyclerView */
    private fun setupRecyclerView() {
        binding.recyclerViewUrnas.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewUrnas.isNestedScrollingEnabled = false
    }

    /** Configura el buscador en tiempo real */
    private fun setupSearch() {
        binding.inputBuscar.addTextChangedListener { query ->
            val texto = query?.toString()?.trim()?.lowercase() ?: ""
            if (::urnaAdapter.isInitialized) {
                urnaAdapter.filter.filter(texto)
            }
        }
    }

    /** Llama a la API para obtener las urnas */
    private fun fetchUrnas() {
        binding.progressBar.visibility = View.VISIBLE
        val service = RetrofitClient.getUrnaService(requireContext())

        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val urnas = response.body() ?: emptyList()
                    urnasOriginales = urnas
                    Log.d("UrnasFragment", "✅ Urnas recibidas: ${urnas.size}")

                    if (urnas.isNotEmpty()) {
                        urnaAdapter = UrnaAdapter(urnas)
                        binding.recyclerViewUrnas.adapter = urnaAdapter

                        // 🔹 Forzar ejecución después de que la vista esté lista
                        binding.dashboardContainer.post {
                            updateDashboard(urnas)
                        }

                    } else {
                        Toast.makeText(requireContext(), "No hay urnas disponibles", Toast.LENGTH_SHORT).show()
                        binding.dashboardContainer.removeAllViews()
                        binding.recyclerViewUrnas.adapter = null
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error ${response.code()}: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                if (_binding == null) return
                binding.progressBar.visibility = View.GONE
                Log.e("UrnasFragment", "❌ Fallo al cargar urnas", t)
                Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /** Convierte dp → px */
    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    /** Actualiza el dashboard con las métricas */
    private fun updateDashboard(urnas: List<Urna>) {
        Log.d("UrnasFragment", "🟨 Entrando a updateDashboard() - urnas=${urnas.size}")
        if (!isAdded || _binding == null || context == null) return

        val container = binding.dashboardContainer
        container.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        val totalUrnas = urnas.size
        val stockTotal = urnas.sumOf { it.stock ?: 0 }
        val preciosValidos = urnas.mapNotNull { it.price }
        val promedioPrecio = if (preciosValidos.isNotEmpty()) preciosValidos.average() else 0.0
        val bajoStock = urnas.count { (it.stock ?: 0) <= 5 }

        val metrics = listOf(
            Triple(totalUrnas.toString(), "Total Urnas", R.drawable.ic_inventory),
            Triple(stockTotal.toString(), "Stock Total", R.drawable.ic_trending_up),
            Triple("$${"%.0f".format(promedioPrecio)}", "Precio Prom.", R.drawable.ic_sales),
            Triple(bajoStock.toString(), "Bajo Stock", R.drawable.ic_warning)
        )

        metrics.forEach { (value, label, iconRes) ->
            try {
                val cardView = inflater.inflate(R.layout.item_metric_card, binding.dashboardContainer, false)

                val params = androidx.gridlayout.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(
                        8.dpToPx(requireContext()),
                        8.dpToPx(requireContext()),
                        8.dpToPx(requireContext()),
                        8.dpToPx(requireContext())
                    )
                }
                cardView.layoutParams = params

                // Asigna valores
                cardView.findViewById<ImageView>(R.id.imgMetricIcon)?.setImageResource(iconRes)
                cardView.findViewById<TextView>(R.id.txtMetricValue)?.text = value
                cardView.findViewById<TextView>(R.id.txtMetricLabel)?.text = label

                // Fondo visible (para verificar render)
                cardView.setBackgroundColor(0x44222222)

                binding.dashboardContainer.addView(cardView)

            } catch (e: Exception) {
                Log.e("UrnasFragment", "Error inflando dashboard", e)
            }
        }
        Log.d("UrnasFragment", "✅ Dashboard actualizado: ${binding.dashboardContainer.childCount} tarjetas")

        container.requestLayout()
        container.invalidate()
        Log.d("UrnasFragment", "✅ Dashboard actualizado: ${container.childCount} tarjetas")
        Log.d("UrnasFragment", "🟩 Dashboard finalizado - hijos=${binding.dashboardContainer.childCount}")

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}