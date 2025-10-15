package com.example.prueba2appurnas.util

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.model.ImageUrl

object NetUtils {

    fun buildAbsoluteUrl(pathOrUrl: String?): String? {
        if (pathOrUrl.isNullOrBlank()) return null
        val raw = pathOrUrl.trim()

        fun String.appendToHost(): String {
            val normalized = if (startsWith("/")) this else "/$this"
            return ApiConfig.BASE_HOST.trimEnd('/') + normalized
        }

        return when {
            raw.startsWith("http", ignoreCase = true) -> raw
            raw.startsWith("/vault/", ignoreCase = true) -> raw.appendToHost()
            raw.startsWith("vault/", ignoreCase = true) -> raw.appendToHost()
            raw.startsWith("/uploads/", ignoreCase = true) -> raw.appendToHost()
            raw.startsWith("uploads/", ignoreCase = true) -> raw.appendToHost()
            raw.startsWith("/storage/", ignoreCase = true) -> raw.appendToHost()
            raw.startsWith("storage/", ignoreCase = true) -> raw.appendToHost()
            raw.startsWith("/") -> raw.appendToHost()
            else -> ApiConfig.BASE_URL_V1.trimEnd('/') + "/" + raw.trimStart('/')
        }
    }

    fun buildAbsoluteUrl(imageUrl: ImageUrl?): String? {
        if (imageUrl == null) return null

        val candidates = buildList {
            imageUrl.path?.let { add(it) }

            val meta = imageUrl.meta
            if (meta != null) {
                val possibleKeys = listOf(
                    "url",
                    "download_url",
                    "downloadURL",
                    "signed_url",
                    "signedUrl",
                    "public_url",
                    "publicUrl"
                )

                possibleKeys.forEach { key ->
                    (meta[key] as? String)?.let { add(it) }
                }
            }
        }

        return candidates.firstNotNullOfOrNull { buildAbsoluteUrl(it) }
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
