package com.example.prueba2appurnas.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaService
import com.example.prueba2appurnas.model.Urna
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dashboardContainer: LinearLayout
    private lateinit var adapter: UrnaAdapter
    private lateinit var service: UrnaService
    private lateinit var btnRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicialización de vistas
        btnRefresh = findViewById(R.id.btnRefresh)
        recyclerView = findViewById(R.id.recyclerViewUrnas)
        dashboardContainer = findViewById(R.id.dashboardContainer)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ Inicializar adapter vacío para evitar crash antes de cargar datos
        adapter = UrnaAdapter(
            mutableListOf(),
            onEdit = { editarUrna(it) },
            onDelete = { eliminarUrna(it) }
        )
        recyclerView.adapter = adapter

        // Inicializar servicio de urnas
        service = RetrofitClient.getUrnaService(this)

        // Botón para recargar urnas
        btnRefresh.setOnClickListener {
            fetchUrnas()
        }

        // Cargar urnas al iniciar
        fetchUrnas()
    }

    // ✅ Obtener urnas desde el backend
    private fun fetchUrnas() {
        btnRefresh.isEnabled = false
        Toast.makeText(this, "Cargando urnas...", Toast.LENGTH_SHORT).show()

        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                btnRefresh.isEnabled = true

                if (response.isSuccessful) {
                    val urnas = response.body()?.toMutableList() ?: mutableListOf()
                    Log.d("HomeActivity", "🟢 ${urnas.size} urnas cargadas correctamente")

                    adapter.updateList(urnas)
                    updateDashboard(urnas)

                    if (urnas.isEmpty()) {
                        Toast.makeText(this@HomeActivity, "No hay urnas registradas", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("HomeActivity", "❌ Error HTTP ${response.code()}")
                    Toast.makeText(this@HomeActivity, "Error ${response.code()} al obtener urnas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                btnRefresh.isEnabled = true
                Log.e("HomeActivity", "🚨 Fallo de conexión: ${t.message}")
                Toast.makeText(this@HomeActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // ✅ Acción al presionar "editar"
    private fun editarUrna(urna: Urna) {
        Toast.makeText(this, "Editar: ${urna.name}", Toast.LENGTH_SHORT).show()
        // 👉 Aquí podrías abrir EditUrnaActivity si la implementas:
        // val intent = Intent(this, EditUrnaActivity::class.java)
        // intent.putExtra("urna", urna)
        // startActivity(intent)
    }

    // ✅ Acción al presionar "eliminar"
    private fun eliminarUrna(urna: Urna) {
        urna.id?.let { id ->
            service.deleteUrna(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        adapter.removeUrna(urna)
                        updateDashboard(adapter.getUrnas())
                        Toast.makeText(this@HomeActivity, "Urna eliminada correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeActivity, "Error al eliminar (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } ?: Toast.makeText(this, "ID de urna inválido", Toast.LENGTH_SHORT).show()
    }

    // ✅ Actualiza el dashboard con métricas simples
    private fun updateDashboard(urnas: List<Urna>) {
        dashboardContainer.removeAllViews()
        val inflater = layoutInflater

        val totalUrnas = urnas.size
        val stockTotal = urnas.sumOf { it.stock ?: 0 }
        val bajoStock = urnas.count { (it.stock ?: 0) <= 5 }
        val ventasMes = (totalUrnas * 65000) / 10 // valor simulado para demostración

        val metrics = listOf(
            Pair(totalUrnas.toString(), "Total de urnas"),
            Pair(stockTotal.toString(), "Stock disponible"),
            Pair(bajoStock.toString(), "Bajo stock"),
            Pair("$${ventasMes}", "Ventas del mes")
        )

        metrics.forEach { (value, label) ->
            val view = inflater.inflate(R.layout.item_metric_card, dashboardContainer, false)
            view.findViewById<TextView>(R.id.txtMetricValue).text = value
            view.findViewById<TextView>(R.id.txtMetricLabel).text = label
            dashboardContainer.addView(view)
        }
    }
}
