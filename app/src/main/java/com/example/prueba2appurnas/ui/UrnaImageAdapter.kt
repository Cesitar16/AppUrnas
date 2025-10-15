package com.example.prueba2appurnas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.UrnaImage
import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.util.NetUtils

class UrnaImageAdapter(
    private val images: List<UrnaImage>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<UrnaImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
    }

    private fun buildAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        return if (pathOrUrl.startsWith("http", true)) pathOrUrl
        else ApiConfig.BASE_URL_V1.trimEnd('/') + "/" + pathOrUrl.trimStart('/')
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
        } else absoluteUrl
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_urna_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = images[position]
        val full  = NetUtils.buildAbsoluteUrl(image.url?.url)
        val model = full?.let { NetUtils.glideModelWithAuth(holder.itemView.context, it) }

        Glide.with(holder.itemView.context)
            .load(model)
            .placeholder(R.drawable.bg_image_border)
            .error(R.drawable.bg_image_border)
            .into(holder.imgThumbnail)

        holder.itemView.setOnClickListener {
            full?.let { onImageClick(it) }
        }
    }

    override fun getItemCount(): Int = images.size
}
