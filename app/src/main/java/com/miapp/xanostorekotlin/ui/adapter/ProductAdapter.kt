package com.miapp.xanostorekotlin.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.miapp.xanostorekotlin.R
import com.miapp.xanostorekotlin.databinding.ItemProductBinding
import com.miapp.xanostorekotlin.model.Product

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.productName.text = product.name
            binding.productPrice.text = product.price?.let { price ->
                binding.root.context.getString(R.string.price_format, price)
            } ?: binding.root.context.getString(R.string.price_unknown)
            binding.productDescription.text = product.description.orEmpty()
            val firstImage = product.images.firstOrNull()?.url
            binding.productImage.load(firstImage) {
                crossfade(true)
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.placeholder_image)
            }
            binding.root.setOnClickListener { onProductClick(product) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean = oldItem == newItem
        }
    }
}
