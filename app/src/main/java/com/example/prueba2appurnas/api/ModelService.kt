package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.Model
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ModelService {
    @GET("model")
    fun getAllModels(): Call<List<Model>>

    @GET("model/{id}")
    fun getModelById(@Path("id") id: Int): Call<Model>
}
