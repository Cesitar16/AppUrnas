package com.example.prueba2appurnas.ui.client.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.prueba2appurnas.databinding.ItemCartBinding
import com.example.prueba2appurnas.model.CartItem

class CartAdapter(
    private var items: MutableList<CartItem>,
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onDelete: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        val b = holder.b

        val qty = item.quantity ?: 0
        val price = item.unit_price ?: 0.0

        // Nombre seguro (si no tiene nombre, mostramos por ID)
        val title = item._urn?.name ?: "Urna #${item.urn_id}"

        b.txtName.text = title
        b.txtQty.text = qty.toString()
        b.txtSubtotal.text = "$${String.format("%.2f", price * qty)}"

        // Cargar imagen si existe
        val imgUrl = item._urn?.image_url?.url
        if (!imgUrl.isNullOrBlank()) {
            Glide.with(b.imgItem.context)
                .load(imgUrl)
                .centerCrop()
                .into(b.imgItem)
        }

        // Aumentar cantidad
        b.btnIncrease.setOnClickListener {
            onIncrease(item)
        }

        // Disminuir cantidad
        b.btnDecrease.setOnClickListener {
            if (qty > 1) onDecrease(item)
        }

        // Eliminar ítem
        b.btnDelete.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
