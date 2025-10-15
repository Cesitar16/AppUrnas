package com.example.prueba2appurnas.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private fun createClient(
        baseUrl: String,
        context: Context,
        withAuth: Boolean
    ): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logging)

        if (withAuth) {
            val tokenManager = TokenManager(context.applicationContext)
            clientBuilder.addInterceptor(AuthInterceptor(tokenManager))
        }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    // 🔸 API para autenticación
    fun getAuthService(context: Context): AuthService {
        return createClient(
            baseUrl = ApiConfig.BASE_URL_AUTH,
            context = context,
            withAuth = false
        ).create(AuthService::class.java)
    }

    // 🔸 API para urnas y demás (grupo v1)
    fun getUrnaService(context: Context): UrnaService {
        return createClient(
            baseUrl = ApiConfig.BASE_URL_V1,
            context = context,
            withAuth = true
        ).create(UrnaService::class.java)
    }

    fun getMaterialService(context: Context): MaterialService {
        return createClient(
            baseUrl = ApiConfig.BASE_URL_V1,
            context = context,
            withAuth = true
        ).create(MaterialService::class.java)
    }

    fun getModelService(context: Context): ModelService {
        return createClient(
            baseUrl = ApiConfig.BASE_URL_V1,
            context = context,
            withAuth = true
        ).create(ModelService::class.java)
    }

    fun getUrnaImageService(context: Context): UrnaImageService {
        return createClient(
            baseUrl = ApiConfig.BASE_URL_V1,
            context = context,
            withAuth = true
        ).create(UrnaImageService::class.java)
    }

}
