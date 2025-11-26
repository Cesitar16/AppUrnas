package com.example.prueba2appurnas.model

data class CreateCartRequest(
    val status: String,
    val updated_at: Int = 0,
    val user_id: Int
)
