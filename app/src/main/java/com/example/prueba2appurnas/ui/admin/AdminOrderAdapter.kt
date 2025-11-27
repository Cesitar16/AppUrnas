package com.example.prueba2appurnas.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prueba2appurnas.R
import com.example.prueba2appurnas.model.OrderResponse

class AdminOrderAdapter(
    private var orders: List<OrderResponse> = emptyList(),
    private val onApprove: (OrderResponse) -> Unit,
    private val onReject: (OrderResponse) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtId: TextView = view.findViewById(R.id.txtOrderId)
        val txtUser: TextView = view.findViewById(R.id.txtOrderUser)
        val txtTotal: TextView = view.findViewById(R.id.txtOrderTotal)
        val txtStatus: TextView = view.findViewById(R.id.txtOrderStatus)
        val btnAccept: Button = view.findViewById(R.id.btnAcceptOrder)
        val btnReject: Button = view.findViewById(R.id.btnRejectOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.txtId.text = "Orden #${order.id}"
        holder.txtUser.text = "Usuario: ${order.user_id}"
        holder.txtTotal.text = "Total: $${order.total}"
        holder.txtStatus.text = "Estado: ${order.status}"

        holder.btnAccept.setOnClickListener { onApprove(order) }
        holder.btnReject.setOnClickListener { onReject(order) }
    }

    override fun getItemCount(): Int = orders.size

    fun submitList(newOrders: List<OrderResponse>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }
}
