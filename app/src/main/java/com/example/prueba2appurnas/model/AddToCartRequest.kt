package com.example.prueba2appurnas.model

data class AddToCartRequest(
    val quantity: Int,
    val unit_price: Double,
    val updated_at: Long = System.currentTimeMillis(),
    val cart_id: Int,
    val urn_id: Int
)