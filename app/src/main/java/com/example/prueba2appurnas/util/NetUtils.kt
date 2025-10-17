package com.example.prueba2appurnas.util

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.api.TokenManager

object NetUtils {

    // 🔹 Crea URL completa desde path relativo o absoluto
    fun buildAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        val raw = pathOrUrl.trim()

    fun buildAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        val raw = pathOrUrl.trim()
        return when {
            raw.startsWith("http", ignoreCase = true) -> raw
            raw.startsWith("/vault/") -> ApiConfig.BASE_HOST.trimEnd('/') + raw
            else -> ApiConfig.BASE_URL_V1.trimEnd('/') + "/" + raw.trimStart('/')
        }
    }


    // 🔹 Crea GlideUrl con header Authorization si el token existe
    fun glideModelWithAuth(context: Context, absoluteUrl: String): Any {
        val token = TokenManager(context).getToken()

        // Si el endpoint es público, no añadimos headers
        if (absoluteUrl.contains("storage.googleapis.com", true) ||
            token.isNullOrBlank()
        ) {
            return absoluteUrl
        }

        // Si el token existe, añadimos Authorization
        return GlideUrl(
            absoluteUrl,
            LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        )
    fun glideModelWithAuth(context: Context, absoluteUrl: String): Any {
        val token = TokenManager(context).getToken()
        return if (!token.isNullOrBlank()) {
            GlideUrl(
                absoluteUrl,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $token") // usa aquí el mismo header que tu AuthInterceptor
                    .build()
            )
        } else {
            absoluteUrl
        }
    }
}
