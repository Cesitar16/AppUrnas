package com.example.prueba2appurnas.ui.client.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.databinding.ItemCartBinding
import com.example.prueba2appurnas.model.CartItem
import com.example.prueba2appurnas.util.NetUtils

class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items = mutableListOf<CartItem>()

    fun submitList(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {

            // Nombre desde "_urn"
            binding.txtCartItemName.text = item._urn?.name ?: "Sin nombre"

            binding.txtCartItemQuantity.text = "x${item.quantity}"
            binding.txtCartItemPrice.text = "$${item.unit_price}"

            // Imagen
            val url = NetUtils.buildAbsoluteUrl(item._urn?.image_url?.path)

            Glide.with(binding.imgCartItem.context)
                .load(url)
                .into(binding.imgCartItem)
        }
    }
}
