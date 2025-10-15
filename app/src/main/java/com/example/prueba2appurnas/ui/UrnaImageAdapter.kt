package com.example.prueba2appurnas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.UrnaImage

class UrnaImageAdapter(
    private val images: List<UrnaImage>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<UrnaImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        Glide.with(holder.itemView.context)
            .load(image.url?.url)
            .into(holder.imgThumbnail)

        holder.itemView.setOnClickListener {
            image.url?.url?.let { url -> onImageClick(url) }
        }
    }

    override fun getItemCount(): Int = images.size
}
