package com.example.prueba2appurnas.model

data class OrderRequest(
    val items_total: Double = 0.0,
    val discount_total: Double = 0.0,
    val grand_total: Double = 0.0,
    val total: Double = 0.0,
    val status: String = "PENDING",

    val shipping_full_name: String = "",
    val shipping_phone: String = "",
    val shipping_line1: String = "",
    val shipping_line2: String = "",
    val shipping_city: String = "",
    val shipping_state: String = "",
    val shipping_postal_code: String = "",
    val shipping_country: String = "",
    val promotion_code: String = "",

    val updated_at: Long = System.currentTimeMillis(),
    val user_id: Int,
    val cart_id: Int,
    val promotion_id: Int = 0
)
