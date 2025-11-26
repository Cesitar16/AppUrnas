package com.example.prueba2appurnas.model

data class CreateCartRequest(
    val status: String = "OPEN",
    val updated_at: Long = System.currentTimeMillis(),
    val user_id: Int
)