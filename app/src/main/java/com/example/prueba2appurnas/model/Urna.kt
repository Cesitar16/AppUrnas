package com.example.prueba2appurnas.model

import java.io.Serializable

data class Urna(
    val id: Int? = null,
    val internal_id: String? = null,
    val name: String? = null,
    val short_description: String? = null,
    val detailed_description: String? = null,
    val height: Double? = null,
    val width: Double? = null,
    val depth: Double? = null,
    val weight: Double? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val available: Boolean? = null,
    val rating_avg: Double? = null,
    val rating_count: Int? = null,
    val updated_at: Long? = null,
    val color_id: Int? = null,
    val material_id: Int? = null,
    val model_id: Int? = null,
    val image_url: ImageUrl? = null
) : Serializable {
    val mainImageUrl: String?
        get() = image_url?.resolvedUrl
}

data class ImageUrl(
    val access: String? = null,
    val path: String? = null,
    val name: String? = null,
    val type: String? = null,
    val size: Int? = null,
    val mime: String? = null,
    val url: String? = null
) : Serializable {
    val resolvedUrl: String?
        get() = when {
            !url.isNullOrBlank() -> url
            !access.isNullOrBlank() && !path.isNullOrBlank() -> {
                val normalizedAccess = access.trimEnd('/')
                val normalizedPath = path.trimStart('/')
                "$normalizedAccess/$normalizedPath"
            }
            !path.isNullOrBlank() -> path
            else -> null
        }
}
