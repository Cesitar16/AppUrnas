package com.example.prueba2appurnas.model

data class OrderResponse(
    val id: Int,
    val created_at: String,
    val items_total: Double,
    val discount_total: Double,
    val grand_total: Double,
    val total: Double,
    val status: String,

    val shipping_full_name: String?,
    val shipping_phone: String?,
    val shipping_line1: String?,
    val shipping_line2: String?,
    val shipping_city: String?,
    val shipping_state: String?,
    val shipping_postal_code: String?,
    val shipping_country: String?,
    val promotion_code: String?,

    val updated_at: Long?,
    val user_id: Int?,
    val cart_id: Int?,
    val promotion_id: Int?
)
