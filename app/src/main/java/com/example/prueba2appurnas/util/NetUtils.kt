package com.example.prueba2appurnas.util

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.prueba2appurnas.api.ApiConfig
import com.example.prueba2appurnas.api.TokenManager
import com.example.prueba2appurnas.model.ImageUrl
import com.example.prueba2appurnas.model.UrlObject

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

        val candidates = LinkedHashSet<String>()
        imageUrl.path?.let { candidates.add(it) }
        imageUrl.name?.takeIf { isLikelyUrlCandidate(it) }?.let { candidates.add(it) }

        imageUrl.meta?.let { meta ->
            collectMetaCandidates(meta).forEach { candidate ->
                if (candidate.isNotBlank()) {
                    candidates.add(candidate)
                }
            }
        }

        return candidates.firstNotNullOfOrNull { buildAbsoluteUrl(it) }
    }

    fun buildAbsoluteUrl(urlObject: UrlObject?): String? {
        return urlObject?.url?.let { buildAbsoluteUrl(it) }
    }

    fun glideModelOrNull(context: Context, imageUrl: ImageUrl?): Any? {
        return buildAbsoluteUrl(imageUrl)?.let { glideModelWithAuth(context, it) }
    }

    fun glideModelOrNull(context: Context, pathOrUrl: String?): Any? {
        return buildAbsoluteUrl(pathOrUrl)?.let { glideModelWithAuth(context, it) }
    }

    fun glideModelOrNull(context: Context, urlObject: UrlObject?): Any? {
        return buildAbsoluteUrl(urlObject)?.let { glideModelWithAuth(context, it) }
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

    private fun collectMetaCandidates(node: Any?, keyHint: String? = null): Sequence<String> {
        return when (node) {
            is String -> {
                val keyMatches = keyHint?.let {
                    it.contains("url", ignoreCase = true) || it.contains("path", ignoreCase = true)
                } ?: false

                if (keyMatches || isLikelyUrlCandidate(node)) {
                    sequenceOf(node)
                } else {
                    emptySequence()
                }
            }

            is Map<*, *> -> {
                node.entries.asSequence().flatMap { entry ->
                    val key = entry.key?.toString()
                    val combinedKey = if (!keyHint.isNullOrBlank()) {
                        "$keyHint.$key"
                    } else {
                        key
                    }
                    collectMetaCandidates(entry.value, combinedKey)
                }
            }

            is Iterable<*> -> node.asSequence().flatMap { item ->
                collectMetaCandidates(item, keyHint)
            }

            is Array<*> -> node.asSequence().flatMap { item ->
                collectMetaCandidates(item, keyHint)
            }

            else -> emptySequence()
        }
    }

    private fun isLikelyUrlCandidate(value: String): Boolean {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.startsWith("http", ignoreCase = true)) return true
        if (trimmed.startsWith("/")) return true

        val lower = trimmed.lowercase()
        if (lower.startsWith("vault/")) return true
        if (lower.startsWith("uploads/")) return true
        if (lower.startsWith("storage/")) return true

        return trimmed.contains('.')
    }
}
