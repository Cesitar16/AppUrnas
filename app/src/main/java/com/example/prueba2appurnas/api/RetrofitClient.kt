package com.example.prueba2appurnas.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Objeto Singleton para configurar y proveer instancias de Retrofit y los servicios API.
 */
object RetrofitClient {

    // Configuración del cliente OkHttp base (con logging y timeouts)
    private fun createBaseOkHttpClient(): OkHttpClient.Builder {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Nivel BODY es muy útil para depurar, muestra cabeceras y cuerpo de request/response
            // Cambia a Level.BASIC o Level.NONE en producción si es necesario
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Añade el interceptor de logging
            .connectTimeout(30, TimeUnit.SECONDS) // Tiempo de espera para conectar
            .readTimeout(30, TimeUnit.SECONDS)    // Tiempo de espera para leer datos
            .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo de espera para escribir datos
    }

    /**
     * Crea una instancia de Retrofit con un cliente OkHttp que INCLUYE el interceptor de autenticación.
     * Ideal para los endpoints que requieren token.
     * @param baseUrl La URL base para esta instancia de Retrofit.
     * @param context Contexto necesario para inicializar TokenManager y AuthInterceptor.
     */
    private fun createAuthenticatedClient(baseUrl: String, context: Context): Retrofit {
        val tokenManager = TokenManager(context)
        val authInterceptor = AuthInterceptor(tokenManager) // Interceptor que añade el token

        val okHttpClient = createBaseOkHttpClient()
            .addInterceptor(authInterceptor) // Añade el interceptor de autenticación
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient) // Usa el cliente con autenticación
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para convertir JSON
            .build()
    }

    /**
     * Crea una instancia de Retrofit con un cliente OkHttp que NO incluye el interceptor de autenticación.
     * Ideal para endpoints públicos o el de login/registro.
     * @param baseUrl La URL base para esta instancia de Retrofit.
     */
    private fun createUnauthenticatedClient(baseUrl: String): Retrofit {
        val okHttpClient = createBaseOkHttpClient()
            // NO se añade el AuthInterceptor aquí
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient) // Usa el cliente sin autenticación
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- Funciones Públicas para Obtener los Servicios ---

    /**
     * Obtiene una instancia de AuthService (para login/registro).
     * Usa la URL base de autenticación y un cliente SIN token automático.
     */
    fun getAuthService(context: Context): AuthService {
        return createUnauthenticatedClient(ApiConfig.BASE_URL_AUTH)
            .create(AuthService::class.java)
    }

    /**
     * Obtiene una instancia de UrnaService (para operaciones CRUD de urnas).
     * Usa la URL base principal (V1) y un cliente CON token automático.
     */
    fun getUrnaService(context: Context): UrnaService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UrnaService::class.java)
    }

    /**
     * Obtiene una instancia de UploadService (para subir imágenes al endpoint genérico).
     * Usa la URL base principal (V1) y un cliente CON token automático.
     */
    fun getUploadService(context: Context): UploadService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UploadService::class.java)
    }

    /**
     * Obtiene una instancia de UrnaImageService (para obtener/añadir imágenes de la galería de una urna).
     * Usa la URL base principal (V1) y un cliente CON token automático.
     */
    fun getUrnaImageService(context: Context): UrnaImageService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(UrnaImageService::class.java)
    }

    /**
     * Obtiene una instancia de ColorService.
     * Usa la URL base principal (V1) y un cliente CON token automático.
     */
    fun getColorService(context: Context): ColorService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(ColorService::class.java)
    }

    /**
     * Obtiene una instancia de MaterialService.
     * Usa la URL base principal (V1) y un cliente CON token automático.
     */
    fun getMaterialService(context: Context): MaterialService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(MaterialService::class.java)
    }

    /**
     * Obtiene una instancia de ModelService.
     * Usa la URL base principal (V1) y un cliente CON token automático.
     */
    fun getModelService(context: Context): ModelService {
        return createAuthenticatedClient(ApiConfig.BASE_URL_V1, context)
            .create(ModelService::class.java)
    }

}