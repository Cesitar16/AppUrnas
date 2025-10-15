package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.model.AuthResponse
import com.miapp.xanostorekotlin.model.LoginRequest
import com.miapp.xanostorekotlin.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("me")
    suspend fun getMe(): User
}
