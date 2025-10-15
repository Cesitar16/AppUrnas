package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.Material
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MaterialService {
    @GET("material")
    fun getAllMaterials(): Call<List<Material>>

    @GET("material/{id}")
    fun getMaterialById(@Path("id") id: Int): Call<Material>
}
