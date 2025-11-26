package com.example.prueba2appurnas.model

data class UpdateCartRequest(
    val status: String?,
    val updated_at: Long = System.currentTimeMillis(),
    val user_id: Int
)