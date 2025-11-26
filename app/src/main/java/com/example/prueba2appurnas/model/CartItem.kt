package com.example.prueba2appurnas.model

data class CartItem(
    val id: Int,
    val quantity: Int,
    val unit_price: Double,
    val updated_at: Long?,
    val cart_id: Int,
    val urn_id: Int,

    // 🔥 IMPORTANTE: agregar objeto expandido que viene desde Xano:
    val _urn: Urna? = null
)

data class UrnaSummary(
    val id: Int,
    val name: String?,
    val image_url: String?
)
