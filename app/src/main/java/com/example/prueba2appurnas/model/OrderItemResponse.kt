package com.example.prueba2appurnas.model

data class OrderItemResponse(
    val id: Int,
    val created_at: String,
    val quantity: Int,
    val unit_price: Double,
    val subtotal: Double,
    val order_id: Int,
    val urn_id: Int
)
