package com.example.prueba2appurnas.ui

import android.os.Bundle
import android.view.LayoutInflater
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
    private lateinit var btnProfile: Button
    private lateinit var adapter: UrnaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recyclerView = findViewById(R.id.recyclerViewUrnas)
        dashboardContainer = findViewById(R.id.dashboardContainer)
        btnProfile = findViewById(R.id.btnProfile)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnProfile.setOnClickListener {
            ProfileFragment().show(supportFragmentManager, ProfileFragment.TAG)
        }

        fetchUrnas()
    }

    private fun fetchUrnas() {
        val service = RetrofitClient.getUrnaService(this)
        service.getUrnas().enqueue(object : Callback<List<Urna>> {
            override fun onResponse(call: Call<List<Urna>>, response: Response<List<Urna>>) {
                if (response.isSuccessful) {
                    val urnas = response.body() ?: emptyList()
                    adapter = UrnaAdapter(urnas)
                    recyclerView.adapter = adapter

                    updateDashboard(urnas)
                } else {
                    Toast.makeText(this@HomeActivity, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Urna>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateDashboard(urnas: List<Urna>) {
        dashboardContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val totalUrnas = urnas.size
        val stockTotal = urnas.sumOf { it.stock ?: 0 }
        val promedioPrecio = if (urnas.isNotEmpty()) urnas.mapNotNull { it.price }.average() else 0.0
        val disponibles = urnas.count { it.available == true }

        val metrics = listOf(
            Pair(totalUrnas.toString(), "Total de urnas"),
            Pair(stockTotal.toString(), "Stock total"),
            Pair("$${promedioPrecio.toInt()}", "Precio promedio"),
            Pair(disponibles.toString(), "Urnas disponibles")
        )

        metrics.forEach { (value, label) ->
            val view = inflater.inflate(R.layout.item_metric_card, dashboardContainer, false)
            view.findViewById<TextView>(R.id.txtMetricValue).text = value
            view.findViewById<TextView>(R.id.txtMetricLabel).text = label
            dashboardContainer.addView(view)
        }
    }
}
