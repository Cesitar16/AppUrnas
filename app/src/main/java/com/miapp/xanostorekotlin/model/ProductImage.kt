package com.miapp.xanostorekotlin.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ProductImage(
    @SerializedName("id") val id: String? = null,
    @SerializedName("url") val url: String,
    @SerializedName("path") val path: String? = null,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("meta") val meta: Map<String, String>? = null
) : Serializable
