package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("images") val images: List<ProductImage> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null
) : Serializable
