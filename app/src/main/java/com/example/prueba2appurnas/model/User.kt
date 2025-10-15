package com.example.prueba2appurnas.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int = -1,
    @SerializedName(value = "name", alternate = ["username"])
    val name: String = "",
    val email: String = "",
    @SerializedName(value = "role", alternate = ["rol"])
    val role: String? = null
)
