package com.example.prueba2appurnas.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName(value = "authToken", alternate = ["auth_token", "token"])
    val authToken: String = "",
    val user: User? = null
)
