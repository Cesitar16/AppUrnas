package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.BuildConfig

object ApiConfig {
    val storeBaseUrl: String
        get() = normalizeBaseUrl(BuildConfig.STORE_BASE_URL)

    val authBaseUrl: String
        get() = normalizeBaseUrl(BuildConfig.AUTH_BASE_URL)

    val tokenTtlSec: Int
        get() = BuildConfig.TOKEN_TTL_SEC

    private fun normalizeBaseUrl(raw: String): String {
        if (raw.isBlank()) return raw
        return if (raw.endsWith('/')) raw else "$raw/"
    }
}
