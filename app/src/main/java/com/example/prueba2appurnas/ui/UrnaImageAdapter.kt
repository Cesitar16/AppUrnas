package com.example.prueba2appurnas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.UrnaImage
import com.example.prueba2appurnas.util.NetUtils

class UrnaImageAdapter(
    private val images: List<UrnaImage>,
    private val onImageClick: (Any) -> Unit
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
        val model = NetUtils.glideModelOrNull(holder.itemView.context, image.url)

        Glide.with(holder.itemView.context)
            .load(model)
            .placeholder(R.drawable.bg_image_border)
            .error(R.drawable.bg_image_border)
            .into(holder.imgThumbnail)

        holder.itemView.setOnClickListener {
            model?.let { onImageClick(it) }
        }
    }

    override fun getItemCount(): Int = images.size
}
