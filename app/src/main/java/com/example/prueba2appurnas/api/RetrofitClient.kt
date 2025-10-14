package com.example.prueba2appurnas.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private fun createClient(baseUrl: String, context: Context): Retrofit {
        val tokenManager = TokenManager(context)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // 🔸 API para autenticación
    fun getAuthService(context: Context): AuthService {
        return createClient(ApiConfig.BASE_URL_AUTH, context).create(AuthService::class.java)
    }

    // 🔸 API para urnas y demás (grupo v1)
    fun getUrnaService(context: Context): UrnaService {
        return createClient(ApiConfig.BASE_URL_V1, context).create(UrnaService::class.java)
    }
}
