package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getUser(): Response<User>
}
