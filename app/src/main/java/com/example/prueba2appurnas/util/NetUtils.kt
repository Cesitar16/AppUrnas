package com.example.prueba2appurnas.util

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.api.TokenManager

object NetUtils {

    fun buildAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        val raw = pathOrUrl.trim()
        return when {
            raw.startsWith("http", ignoreCase = true) -> raw
            raw.startsWith("/vault/") -> ApiConfig.BASE_HOST.trimEnd('/') + raw
            else -> ApiConfig.BASE_URL_V1.trimEnd('/') + "/" + raw.trimStart('/')
        }
    }

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
