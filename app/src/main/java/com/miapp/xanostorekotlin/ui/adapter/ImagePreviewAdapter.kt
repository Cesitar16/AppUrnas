package com.miapp.xanostorekotlin.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ItemImagePreviewBinding

class ImagePreviewAdapter : RecyclerView.Adapter<ImagePreviewAdapter.PreviewViewHolder>() {
    private val items = mutableListOf<Uri>()

    fun submitList(newItems: List<Uri>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PreviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PreviewViewHolder(private val binding: ItemImagePreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.previewImage.load(uri) {
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
            }
        }
    }
}
