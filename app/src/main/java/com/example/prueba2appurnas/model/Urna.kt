package com.example.prueba2appurnas.model

import java.io.Serializable

data class Urna(
    val id: Int,
    val internal_id: String?,
    val name: String,
    val short_description: String?,
    val detailed_description: String?,
    val height: Double?,
    val width: Double?,
    val depth: Double?,
    val weight: Double?,
    val price: Double?,
    val stock: Int?,
    val available: Boolean?,
    val rating_avg: Double?,
    val rating_count: Int?,
    val updated_at: Long?,
    val color_id: Int?,
    val material_id: Int?,
    val model_id: Int?,
    val image_url: ImageUrl?
) : Serializable

data class ImageUrl(
    val access: String?,
    val path: String?,
    val name: String?,
    val type: String?,
    val size: Int?,
    val mime: String?,
    val meta: Map<String, Any>?
) : Serializable
