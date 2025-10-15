package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName

data class CreateProductRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("images") val images: List<ProductImage>
)
