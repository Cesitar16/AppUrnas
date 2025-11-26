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
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.ui.fragments.UrnaDetailFragment
import com.example.prueba2appurnas.util.NetUtils

/**
 * Adaptador para la lista de urnas, con soporte de filtrado y clics.
 * Compatible con modo ADMIN 🛠️ y CLIENTE 🛒.
 */
class UrnaAdapter(
    private var urnasOriginal: List<Urna>
) : RecyclerView.Adapter<UrnaAdapter.UrnaViewHolder>(), Filterable {

    // Lista filtrada (inicia igual que la original)
    private var urnasFiltradas: List<Urna> = urnasOriginal.toList()

    // Listener para CLIENTE
    private var itemClickListener: ((Urna) -> Unit)? = null

    fun setOnItemClickListener(listener: (Urna) -> Unit) {
        this.itemClickListener = listener
    }

    // Actualiza los datos cuando llegan desde API
    fun updateList(newList: List<Urna>) {
        urnasOriginal = newList
        urnasFiltradas = newList.toList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrnaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_card, parent, false)
        return UrnaViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrnaViewHolder, position: Int) {
        val urna = urnasFiltradas[position]
        holder.bind(urna)

        // 🔥 CLIENTE → clic delegado por callback
        itemClickListener?.let { listener ->
            holder.itemView.setOnClickListener { listener(urna) }
            return
        }

        // 🔥 ADMIN → abre UrnaDetailFragment
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            if (context is AppCompatActivity) {
                val fragment = UrnaDetailFragment.newInstance(urna)
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(UrnaDetailFragment::class.java.simpleName)
                    .commit()
            } else {
                Log.e("UrnaAdapter", "Contexto inválido")
                Toast.makeText(context, "Error al abrir detalle", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = urnasFiltradas.size

    /**
     * Filtro → busca por nombre o ID
     */
    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(query: CharSequence?): FilterResults {
            val texto = query?.toString()?.trim()?.lowercase() ?: ""

            val resultados = if (texto.isEmpty()) {
                urnasOriginal
            } else {
                urnasOriginal.filter {
                    it.name?.lowercase()?.contains(texto) == true ||
                            it.id.toString().contains(texto)
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
     * ViewHolder → representa cada tarjeta de urna
     */
    class UrnaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.imgUrna)
        private val txtName: TextView = itemView.findViewById(R.id.txtUrnaName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtUrnaPrice)
        private val txtStock: TextView = itemView.findViewById(R.id.txtUrnaStock)

        fun bind(urna: Urna) {
            txtName.text = urna.name ?: "Sin nombre"
            txtPrice.text = "$${urna.price ?: 0.0}"

            val stock = urna.stock ?: 0
            txtStock.text = "Stock: $stock"
            val colorRes = if (stock <= 5) R.color.stockBajo else R.color.stockNormal
            txtStock.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            val fullUrl = NetUtils.buildAbsoluteUrl(urna.image_url?.path)

            Glide.with(itemView.context)
                .load(fullUrl)
                .placeholder(R.drawable.bg_image_border)
                .error(R.drawable.bg_image_border)
                .centerCrop()
                .into(img)
        }
    }
}
