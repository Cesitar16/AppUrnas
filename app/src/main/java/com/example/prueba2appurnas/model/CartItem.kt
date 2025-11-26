package com.example.prueba2appurnas.model

data class CartItem(
    val id: Int,
    val created_at: String?,
    val quantity: Int,
    val unit_price: Double,
    val updated_at: Long?,
    val cart_id: Int,
    val urn_id: Int,
    val _urn: Urna? = null
)
