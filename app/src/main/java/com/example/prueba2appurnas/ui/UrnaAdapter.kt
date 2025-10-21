package com.example.prueba2appurnas.ui

import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.ui.fragments.UrnaDetailFragment
import com.example.prueba2appurnas.util.NetUtils

/**
 * Adaptador para la lista de urnas, con soporte de filtrado y navegación a detalle.
 */
class UrnaAdapter(private val urnasOriginal: List<Urna>) :
    RecyclerView.Adapter<UrnaAdapter.UrnaViewHolder>(), Filterable {

    // Lista filtrada (inicia igual que la original)
    private var urnasFiltradas: List<Urna> = urnasOriginal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrnaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_card, parent, false)
        return UrnaViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrnaViewHolder, position: Int) {
        val urna = urnasFiltradas[position]
        holder.bind(urna)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            if (context is AppCompatActivity) {
                val detailFragment = UrnaDetailFragment.newInstance(urna)

                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(UrnaDetailFragment::class.java.simpleName)
                    .commit()
            } else {
                Log.e("UrnaAdapter", "El contexto no es AppCompatActivity, no se puede navegar a Fragment")
                Toast.makeText(context, "Error de navegación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = urnasFiltradas.size

    /**
     * Implementación del filtro para búsqueda por nombre o ID.
     */
    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(query: CharSequence?): FilterResults {
            val texto = query.toString().trim().lowercase()

            val resultados = if (texto.isEmpty()) {
                urnasOriginal
            } else {
                urnasOriginal.filter {
                    it.name?.lowercase()?.contains(texto) == true ||
                            it.id?.toString()?.contains(texto) == true
                }
            }

            return FilterResults().apply { values = resultados }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            urnasFiltradas = results?.values as? List<Urna> ?: emptyList()
            notifyDataSetChanged()
        }
    }

    /**
     * ViewHolder que representa cada tarjeta de urna.
     */
    class UrnaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUrna: ImageView = itemView.findViewById(R.id.imgUrna)
        private val txtName: TextView = itemView.findViewById(R.id.txtUrnaName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtUrnaPrice)
        private val txtStock: TextView = itemView.findViewById(R.id.txtUrnaStock)

        fun bind(urna: Urna) {
            txtName.text = urna.name ?: "Sin nombre"
            txtPrice.text = "$${urna.price ?: 0.0}"

            // Mostrar stock
            val stock = urna.stock ?: 0
            txtStock.text = "Stock: $stock"

            // Cambiar color según cantidad (verde o rojo)
            val colorRes = if (stock <= 5) R.color.stockBajo else R.color.stockNormal
            txtStock.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            // Obtener URL absoluta de imagen
            val rawPath = urna.image_url?.path
            val fullUrl = NetUtils.buildAbsoluteUrl(rawPath)

            Log.d("IMG_DEBUG", "Urna: ${urna.name}, path: $rawPath, fullUrl: $fullUrl")

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(fullUrl)
                .placeholder(R.drawable.bg_image_border)
                .error(R.drawable.bg_image_border)
                .centerCrop()
                .into(imgUrna)

            urna.image_url?.path?.let { raw ->
                val full  = NetUtils.buildAbsoluteUrl(raw)
                val model = full?.let { NetUtils.glideModelWithAuth(itemView.context, it) }
                Glide.with(itemView.context)
                    .load(model)
                    .placeholder(R.drawable.bg_image_border)
                    .error(R.drawable.bg_image_border)
                    .into(imgUrna)
            }
        }

    }

    private fun buildAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        return if (pathOrUrl.startsWith("http", ignoreCase = true)) {
            pathOrUrl
        } else {
            ApiConfig.BASE_URL_V1.trimEnd('/') + "/" + pathOrUrl.trimStart('/')
        }
    }

    private fun buildGlideModelWithAuth(context: Context, absoluteUrl: String): Any {
        val token = TokenManager(context).getToken()
        return if (!token.isNullOrBlank()) {
            GlideUrl(
                absoluteUrl,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            absoluteUrl
        }
    }



}
