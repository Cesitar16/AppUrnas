package com.example.prueba2appurnas.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.Urna

class UrnaAdapter(
    private val urnas: MutableList<Urna>,
    private val onEdit: (Urna) -> Unit,
    private val onDelete: (Urna) -> Unit
) : RecyclerView.Adapter<UrnaAdapter.UrnaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrnaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_card, parent, false)
        return UrnaViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrnaViewHolder, position: Int) {
        val urna = urnas[position]
        holder.bind(urna, onEdit, onDelete)
    }

    override fun getItemCount(): Int = urnas.size

    class UrnaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUrna: ImageView = itemView.findViewById(R.id.imgUrna)
        private val txtName: TextView = itemView.findViewById(R.id.txtUrnaName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtUrnaPrice)
        private val txtStock: TextView = itemView.findViewById(R.id.txtUrnaStock)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnMenu: ImageView = itemView.findViewById(R.id.btnMenu)

        fun bind(urna: Urna, onEdit: (Urna) -> Unit, onDelete: (Urna) -> Unit) {
            txtName.text = urna.name ?: "Sin nombre"
            txtPrice.text = "$${urna.price ?: 0.0}"
            txtStock.text = "Stock: ${urna.stock ?: 0}"

            // Cambia color del texto según stock
            val color = if ((urna.stock ?: 0) <= 5)
                android.R.color.holo_red_light
            else
                android.R.color.holo_green_light
            txtStock.setTextColor(itemView.context.getColor(color))

            // Cargar imagen
            urna.image_url?.url?.let { imageUrl ->
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_image_border)
                    .error(R.drawable.bg_image_border)
                    .into(imgUrna)
            }

            // ✅ Redirección al detalle (corregido: clave "urna" en minúscula)
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, UrnaDetailActivity::class.java).apply {
                    putExtra("urna", urna)
                }
                context.startActivity(intent)
            }

            btnEdit.setOnClickListener { onEdit(urna) }
            btnMenu.setOnClickListener { onDelete(urna) }
        }
    }

    fun getUrnas(): List<Urna> = urnas

    // ✅ Eliminar urna de la lista
    fun removeUrna(urna: Urna): Int {
        val index = urnas.indexOfFirst { it.id == urna.id }
        if (index != -1) {
            urnas.removeAt(index)
            notifyItemRemoved(index)
        }
        return index
    }

    // ✅ Actualizar lista completa
    fun updateList(newList: List<Urna>) {
        urnas.clear()
        urnas.addAll(newList)
        notifyDataSetChanged()
    }
}
