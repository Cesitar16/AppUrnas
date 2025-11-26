package com.example.prueba2appurnas.model

data class UpdateCartItemRequest(
    val quantity: Int,
    val unit_price: Double,
    val updated_at: Int = 0,
    val cart_id: Int,
    val urn_id: Int
)
