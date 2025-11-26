package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.User
import retrofit2.Response
import retrofit2.http.*

interface UserService {

    // --- Perfil Propio ---
    @PATCH("user/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body userData: Map<String, String>
    ): Response<User>

    // --- Gestión Admin (NUEVOS) ---

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>

    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Unit>

    // 🔥 CORRECCIÓN AQUÍ: Agregamos @JvmSuppressWildcards
    @PATCH("user/{user_id}")
    suspend fun adminUpdateUser(
        @Path("user_id") userId: Int,
        @Body userData: Map<String, @JvmSuppressWildcards Any>
    ): Response<User>
}