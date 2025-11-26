package com.example.prueba2appurnas.model

import com.google.gson.annotations.SerializedName
data class User(
    val id: Int,
    @SerializedName("user") //  Mapea el JSON "user" a la variable "name"
    val name: String,
    val email: String,
    val rol: String
)