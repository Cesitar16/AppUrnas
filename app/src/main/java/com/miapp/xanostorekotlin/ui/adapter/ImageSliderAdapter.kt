package com.miapp.xanostorekotlin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ItemImageSliderBinding
import com.miapp.xanostorekotlin.model.ProductImage

class ImageSliderAdapter(
    private val images: List<ProductImage>
) : RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val binding = ItemImageSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageSliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        val image = images.getOrNull(position)
        holder.bind(image)
    }

    override fun getItemCount(): Int = if (images.isEmpty()) 1 else images.size

    inner class ImageSliderViewHolder(private val binding: ItemImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: ProductImage?) {
            binding.sliderImage.load(image?.url) {
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
            }
        }
    }
}
