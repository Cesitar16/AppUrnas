package com.example.prueba2appurnas.ui

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.Urna
import com.example.prueba2appurnas.util.NetUtils

class UrnaAdapter(private val urnas: List<Urna>) :
    RecyclerView.Adapter<UrnaAdapter.UrnaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrnaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_card, parent, false)
        return UrnaViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrnaViewHolder, position: Int) {
        val urna = urnas[position]
        holder.bind(urna)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, UrnaDetailActivity::class.java).apply {
                putExtra("urn", urna)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = urnas.size

    class UrnaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUrna: ImageView = itemView.findViewById(R.id.imgUrna)
        private val txtName: TextView = itemView.findViewById(R.id.txtUrnaName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtUrnaPrice)

        fun bind(urna: Urna) {
            txtName.text = urna.name ?: "Sin nombre"
            txtPrice.text = "$${urna.price ?: 0.0}"

            // 🧩 Tomamos el path real de la imagen (de ImageUrl)
            val rawPath = urna.image_url?.path
            val fullUrl = NetUtils.buildAbsoluteUrl(rawPath)

            // 🔍 Log para verificar
            Log.d("IMG_DEBUG", "Urna: ${urna.name}, path: $rawPath, fullUrl: $fullUrl")

            // 🖼️ Cargamos imagen con Glide
            Glide.with(itemView.context)
                .load(fullUrl)
                .placeholder(R.drawable.bg_image_border)
                .error(R.drawable.bg_image_border)
                .centerCrop()
                .into(imgUrna)
        }
    }
}
