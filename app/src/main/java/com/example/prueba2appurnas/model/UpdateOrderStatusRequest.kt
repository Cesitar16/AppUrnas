package com.example.prueba2appurnas.model

data class UpdateOrderStatusRequest(
    val status: String,
    val updated_at: Long
)
