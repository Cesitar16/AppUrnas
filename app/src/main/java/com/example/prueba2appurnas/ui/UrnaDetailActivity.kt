package com.example.prueba2appurnas.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.api.UrnaImageService
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.model.UrnaImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UrnaDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URNA = "extra_urna"
    }

    private lateinit var imageUrna: ImageView
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var urnaImageService: UrnaImageService

    private lateinit var tvNombreUrna: TextView
    private lateinit var tvDescripcionCorta: TextView
    private lateinit var tvDescripcionLarga: TextView
    private lateinit var tvPeso: TextView
    private lateinit var tvDisponible: TextView
    private lateinit var tvMaterial: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var tvColor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_urna_detail)

        // Vincular vistas
        imageUrna = findViewById(R.id.imageUrna)
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        tvNombreUrna = findViewById(R.id.tvNombreUrna)
        tvDescripcionCorta = findViewById(R.id.tvDescripcionCorta)
        tvDescripcionLarga = findViewById(R.id.tvDescripcionLarga)
        tvPeso = findViewById(R.id.tvPeso)
        tvDisponible = findViewById(R.id.tvDisponible)
        tvMaterial = findViewById(R.id.tvMaterial)
        tvPrecio = findViewById(R.id.tvPrecio)
        tvColor = findViewById(R.id.tvColor)

        recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // ✅ Obtener objeto Urna del intent (clave corregida)
        val urna = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_URNA, Urna::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_URNA) as? Urna
        }

        if (urna == null) {
            Log.e("UrnaDetailActivity", "⚠️ No se encontró el objeto Urna en el intent")
            Toast.makeText(this, "No se encontró información de la urna", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Log para depuración
        Log.d("UrnaDetailActivity", "🟢 Urna recibida: ${urna.name} (ID: ${urna.id})")

        // Mostrar imagen principal
        Glide.with(this)
            .load(urna.mainImageUrl)
            .transition(DrawableTransitionOptions.withCrossFade(400))
            .placeholder(R.drawable.bg_image_border)
            .error(R.drawable.bg_image_border)
            .centerCrop()
            .into(imageUrna)

        // Mostrar datos
        tvNombreUrna.text = urna.name ?: getString(R.string.placeholder_without_name)
        tvDescripcionCorta.text = urna.short_description ?: "Sin descripción"
        tvDescripcionLarga.text = urna.detailed_description ?: "Sin descripción detallada"
        tvPeso.text = "${urna.weight ?: 0.0} kg"
        tvDisponible.text = if (urna.available == true) "Sí" else "No"
        tvMaterial.text = "Material ID: ${urna.material_id ?: "-"}"
        tvPrecio.text = getString(R.string.placeholder_price, urna.price ?: 0.0)
        tvColor.text = "Color ID: ${urna.color_id ?: "-"}"

        // Cargar imágenes adicionales (si hay endpoint configurado)
        urna.id?.let { fetchUrnaImages(it) }
    }

    private fun fetchUrnaImages(urnaId: Int) {
        urnaImageService = RetrofitClient.getUrnaImageService(this)

        urnaImageService.getImagesByUrnaId(urnaId).enqueue(object : Callback<List<UrnaImage>> {
            override fun onResponse(
                call: Call<List<UrnaImage>>,
                response: Response<List<UrnaImage>>
            ) {
                if (response.isSuccessful) {
                    val images = response.body()?.filter { it.urna_id == urnaId } ?: emptyList()

                    if (images.isNotEmpty()) {
                        val adapter = UrnaImageAdapter(images) { imageUrl ->
                            Glide.with(this@UrnaDetailActivity)
                                .load(imageUrl)
                                .transition(DrawableTransitionOptions.withCrossFade(400))
                                .centerCrop()
                                .into(imageUrna)
                        }
                        recyclerViewImages.adapter = adapter
                    } else {
                        Log.w("UrnaDetailActivity", "⚠️ Sin imágenes asociadas")
                    }
                } else {
                    Log.e("UrnaDetailActivity", "Error al cargar imágenes: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<UrnaImage>>, t: Throwable) {
                Log.e("UrnaDetailActivity", "Fallo al conectar: ${t.message}")
                Toast.makeText(
                    this@UrnaDetailActivity,
                    "Fallo al conectar con el servidor",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
