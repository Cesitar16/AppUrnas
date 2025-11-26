package com.example.prueba2appurnas.model

import java.io.Serializable

data class UrnaImage(
    val id: Int,
    val urna_id: Int,
    val alt: String?,
    val is_cover: Boolean?,
    val sort_order: Int?,
    val url: UrlObject?
) : Serializable

data class UrlObject(
    val url: String
)

