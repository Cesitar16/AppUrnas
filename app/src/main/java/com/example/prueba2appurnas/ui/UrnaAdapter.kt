package com.example.prueba2appurnas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.Urna

class UrnaAdapter(
    private val urnas: List<Urna>,
    private val onItemClick: (Urna) -> Unit
) : RecyclerView.Adapter<UrnaAdapter.UrnaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrnaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_card, parent, false)
        return UrnaViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrnaViewHolder, position: Int) {
        val urna = urnas[position]
        holder.bind(urna, onItemClick)
    }

    override fun getItemCount(): Int = urnas.size

    class UrnaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgUrna: ImageView = itemView.findViewById(R.id.imgUrna)
        private val txtName: TextView = itemView.findViewById(R.id.txtUrnaName)
        private val txtPrice: TextView = itemView.findViewById(R.id.txtUrnaPrice)

        fun bind(urna: Urna, onItemClick: (Urna) -> Unit) {
            txtName.text = urna.name ?: itemView.context.getString(R.string.placeholder_without_name)
            txtPrice.text = itemView.context.getString(R.string.placeholder_price, urna.price ?: 0.0)

            val imageUrl = urna.mainImageUrl
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_image_border)
                .error(R.drawable.bg_image_border)
                .centerCrop()
                .into(imgUrna)

            itemView.setOnClickListener { onItemClick(urna) }
        }
    }
}
