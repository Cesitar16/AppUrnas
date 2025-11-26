package com.example.prueba2appurnas.model

data class CreateCartItemRequest(
    val cart_id: Int,
    val urn_id: Int,
    val quantity: Int,
    val unit_price: Double
)
