package com.miapp.xanostorekotlin.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val TIMEOUT_SECONDS = 30L

    private fun baseClient(context: Context, authenticated: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(logging)

        if (authenticated) {
            builder.addInterceptor(AuthInterceptor(TokenManager(context)))
        }

        return builder.build()
    }

    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun createAuthService(context: Context): AuthService {
        val client = baseClient(context, authenticated = false)
        return retrofit(ApiConfig.authBaseUrl, client).create(AuthService::class.java)
    }

    fun createProductService(context: Context): ProductService {
        val client = baseClient(context, authenticated = true)
        return retrofit(ApiConfig.storeBaseUrl, client).create(ProductService::class.java)
    }

    fun createUploadService(context: Context): UploadService {
        val client = baseClient(context, authenticated = true)
        return retrofit(ApiConfig.storeBaseUrl, client).create(UploadService::class.java)
    }
}
