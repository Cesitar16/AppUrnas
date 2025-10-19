package com.example.prueba2appurnas.ui

import android.content.Intent
import android.graphics.Color
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
import com.example.prueba2appurnas.ui.fragments.EditUrnaFragment
import com.example.prueba2appurnas.util.NetUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UrnaDetailActivity : AppCompatActivity() {

    // 🔹 Vistas
    private lateinit var imageUrna: ImageView
    private lateinit var recyclerViewImages: RecyclerView

    private lateinit var tvNombreUrna: TextView
    private lateinit var tvMedidas: TextView
    private lateinit var tvDescripcionCorta: TextView
    private lateinit var tvDescripcionLarga: TextView
    private lateinit var tvPeso: TextView
    private lateinit var tvStock: TextView
    private lateinit var tvDisponible: TextView
    private lateinit var tvMaterial: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var tvColor: TextView
    private lateinit var btnEditar: Button
    private lateinit var btnEliminar: Button

    private lateinit var urnaImageService: UrnaImageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_urna_detail)

        // 🔹 Vincular vistas
        imageUrna = findViewById(R.id.imageUrna)
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        tvNombreUrna = findViewById(R.id.tvNombreUrna)
        tvMedidas = findViewById(R.id.tvMedidas)
        tvDescripcionCorta = findViewById(R.id.tvDescripcionCorta)
        tvDescripcionLarga = findViewById(R.id.tvDescripcionLarga)
        tvPeso = findViewById(R.id.tvPeso)
        tvStock = findViewById(R.id.tvStock)
        tvDisponible = findViewById(R.id.tvDisponible)
        tvMaterial = findViewById(R.id.tvMaterial)
        tvPrecio = findViewById(R.id.tvPrecio)
        tvColor = findViewById(R.id.tvColor)
        btnEditar = findViewById(R.id.btnEditar)

        recyclerViewImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 🔹 Obtener la urna del Intent
        val urna = intent.getSerializableExtra("urn") as? Urna
        if (urna == null) {
            Log.e("UrnaDetailActivity", "⚠️ No se encontró el objeto Urna en el intent")
            Toast.makeText(this, "No se encontró información de la urna", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("UrnaDetailActivity", "🟢 Urna recibida: ${urna.name} (ID: ${urna.id})")

        // 🖼️ Mostrar imagen principal
        val imagePath = urna.image_url?.path
        val fullUrl = NetUtils.buildAbsoluteUrl(imagePath)
        val glideModel = fullUrl?.let { NetUtils.glideModelWithAuth(this, it) }

        Glide.with(this)
            .load(glideModel)
            .placeholder(R.drawable.bg_image_border)
            .error(R.drawable.bg_image_border)
            .centerCrop()
            .into(imageUrna)

        // 🧾 Mostrar datos generales
        tvNombreUrna.text = urna.name ?: "Sin nombre"
        tvDescripcionCorta.text = urna.short_description ?: "Sin descripción corta"
        tvDescripcionLarga.text = urna.detailed_description ?: "Sin descripción detallada"

        tvMedidas.text =
            "Ancho: ${urna.width ?: 0} cm | Profundidad: ${urna.depth ?: 0} cm | Alto: ${urna.height ?: 0} cm"

        tvPeso.text = "Peso: ${urna.weight ?: 0.0} kg"
        tvStock.text = "Stock: ${urna.stock ?: 0}"
        tvPrecio.text = "Precio: $${urna.price ?: 0.0}"
        tvColor.text = "Color ID: ${urna.color_id ?: "-"}"
        tvMaterial.text = "Material ID: ${urna.material_id ?: "-"}"

        // ✅ Mostrar disponibilidad con color
        val disponible = urna.available == true
        tvDisponible.text = "Disponible: ${if (disponible) "Sí" else "No"}"
        tvDisponible.setTextColor(if (disponible) Color.parseColor("#6FCF97") else Color.parseColor("#EB5757"))

        // 🧩 Botón editar
        btnEditar.setOnClickListener {
            val intent = Intent(this, EditUrnaFragment::class.java)
            intent.putExtra("urn", urna)
            startActivity(intent)
        }

        // 🧨 Botón eliminar (puedes conectar al backend después)
        btnEliminar.setOnClickListener {
            Toast.makeText(this, "Función eliminar pendiente de implementación", Toast.LENGTH_SHORT)
                .show()
        }

        // 📸 Cargar imágenes adicionales
        urna.id.let { fetchUrnaImages(it) }
    }

    // 🔹 Cargar imágenes adicionales de la urna
    private fun fetchUrnaImages(urnaId: Int) {
        urnaImageService = RetrofitClient.getUrnaImageService(this) // <-- CORRECTO: Usa la función pública

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