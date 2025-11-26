package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface UserService {

    /**
     * Actualiza los datos del usuario.
     * Endpoint: PATCH /user/{id}
     * Xano permite enviar parciales (solo los campos que cambian).
     */
    @PATCH("user/{id}")
    suspend fun updateUser(
        @Path("id") userId: Int,
        @Body userData: Map<String, String>
    ): Response<User>
}