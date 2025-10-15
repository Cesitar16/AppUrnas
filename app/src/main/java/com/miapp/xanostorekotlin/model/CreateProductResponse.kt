package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateProductResponse(
    @SerializedName("product") val product: Product?,
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null
) : Serializable
