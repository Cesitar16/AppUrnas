package com.example.prueba2appurnas.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ===============================
    // BASE OKHTTP CLIENT
    // ===============================
    private fun createBaseOkHttpClient(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
    }

    // ===============================
    // CLIENTE AUTENTICADO
    // ===============================
    private fun createAuthenticatedClient(baseUrl: String, context: Context): Retrofit {
        val tokenManager = TokenManager(context)
        val authInterceptor = AuthInterceptor(tokenManager)

        val okHttpClient = createBaseOkHttpClient()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ===============================
    // CLIENTE SIN TOKEN
    // ===============================
    private fun createUnauthenticatedClient(baseUrl: String): Retrofit {
        val okHttpClient = createBaseOkHttpClient().build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ===========================================================
    // 🔥 SERVICIOS (ENDPOINTS)
    // ===========================================================

    // LOGIN / REGISTER
    fun getAuthService(context: Context): AuthService =
        createUnauthenticatedClient(ApiConfig.BASE_URL_AUTH)
            .create(AuthService::class.java)

    // 🔥 USUARIO AUTENTICADO → /auth/me
    fun getAuthenticatedAuthService(context: Context): AuthService =
        createAuthenticatedClient(ApiConfig.BASE_URL_AUTH, context)
            .create(AuthService::class.java)

    // Alias para compatibilidad con tu código anterior
    fun getAuthenticatedUserService(context: Context): AuthService =
        getAuthenticatedAuthService(context)

    // URNAS
    fun getUrnaService(context: Context): UrnaService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UrnaService::class.java)

    // IMÁGENES
    fun getUrnaImageService(context: Context): UrnaImageService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UrnaImageService::class.java)

    // COLORES
    fun getColorService(context: Context): ColorService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(ColorService::class.java)

    // MATERIALES
    fun getMaterialService(context: Context): MaterialService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(MaterialService::class.java)

    // MODELOS
    fun getModelService(context: Context): ModelService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(ModelService::class.java)

    // UPLOAD
    fun getUploadService(context: Context): UploadService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UploadService::class.java)

    fun getCartService(context: Context): CartService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(CartService::class.java)
    }

    // USUARIOS (Para editar perfil)
    fun getUserService(context: Context): UserService =
        createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UserService::class.java)

}
