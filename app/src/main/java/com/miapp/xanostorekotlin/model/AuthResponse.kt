package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthResponse(
    @SerializedName("authToken") val authToken: String,
    @SerializedName("user") val user: User? = null
) : Serializable
