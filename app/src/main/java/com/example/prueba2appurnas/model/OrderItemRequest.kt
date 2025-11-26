package com.example.prueba2appurnas.model

data class OrderItemRequest(
    val order_id: Int,
    val urn_id: Int,
    val quantity: Int,
    val unit_price: Double,
    val subtotal: Double   // <-- AGREGAR ESTO
)
