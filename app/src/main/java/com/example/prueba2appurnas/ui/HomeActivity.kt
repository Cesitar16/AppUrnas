package com.example.prueba2appurnas.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.model.Urna
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dashboardContainer: GridLayout
    private lateinit var adapter: UrnaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerViewUrnas)
        dashboardContainer = findViewById(R.id.dashboardContainer)

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchUrnas()
    }

    /**
     * Obtiene la lista de urnas desde el backend (Xano) usando Retrofit.
     */
    private fun fetchUrnas() {
        val service = RetrofitClient.getUrnaService(this)

        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                if (response.isSuccessful) {
                    val urnas = response.body() ?: emptyList()
                    Log.d("HOME_DEBUG", "Recibidas urnas: ${urnas.size}")

                    if (urnas.isNotEmpty()) {
                        adapter = UrnaAdapter(urnas)
                        recyclerView.adapter = adapter
                        updateDashboard(urnas)
                        setupSearch(urnas)
                    } else {
                        Toast.makeText(this@HomeActivity, "No hay urnas disponibles", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@HomeActivity, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("HOME_ERROR", "Código HTTP: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("HOME_ERROR", "Fallo de red o backend", t)
            }
        })
    }

    private fun setupSearch(urnas: List<Urna>) {
        val inputBuscar = findViewById<android.widget.EditText>(R.id.inputBuscar)

        inputBuscar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()

                // 🔍 Filtrar urnas por nombre o ID interno
                val filtered = urnas.filter { urna ->
                    val nombre = urna.name?.lowercase()?.contains(query) ?: false
                    val internalId = urna.internal_id?.lowercase()?.contains(query) ?: false
                    nombre || internalId
                }

                adapter.updateData(filtered)
            }
        })
    }

    /**
     * Actualiza el dashboard superior con métricas calculadas.
     */
    private fun updateDashboard(urnas: List<Urna>) {
        dashboardContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val totalUrnas = urnas.size
        val stockTotal = urnas.sumOf { it.stock ?: 0 }
        val promedioPrecio = if (urnas.isNotEmpty()) urnas.mapNotNull { it.price }.average() else 0.0

        // 🔸 Contar urnas con stock bajo (5 o menos)
        val urnasBajoStock = urnas.count { (it.stock ?: 0) <= 5 }

        // 🔸 Mostrar las 4 métricas actualizadas
        val metrics = listOf(
            Triple(totalUrnas.toString(), getString(R.string.total_urnas), R.drawable.ic_inventory),
            Triple(stockTotal.toString(), getString(R.string.stock_total), R.drawable.ic_trending_up),
            Triple("$${promedioPrecio.toInt()}", getString(R.string.precio_promedio), R.drawable.ic_sales),
            Triple(urnasBajoStock.toString(), getString(R.string.urnas_bajo_stock), R.drawable.ic_warning)
        )

        dashboardContainer.columnCount = 4

        metrics.forEach { (value, label, iconRes) ->
            val view = inflater.inflate(R.layout.item_metric_card, dashboardContainer, false)

            view.findViewById<TextView>(R.id.txtMetricValue).text = value
            view.findViewById<TextView>(R.id.txtMetricLabel).text = label

            val icon = view.findViewById<ImageView>(R.id.imgMetricIcon)
            icon.setImageResource(iconRes)
            icon.visibility = View.VISIBLE

            // 🔸 Forzar distribución uniforme dentro del GridLayout
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }

            view.layoutParams = params
            dashboardContainer.addView(view)
        }
    }

}
