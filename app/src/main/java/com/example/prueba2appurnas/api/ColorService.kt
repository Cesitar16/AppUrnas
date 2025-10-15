package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.Color
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ColorService {
    @GET("color")
    fun getAllColors(): Call<List<Color>>

    @GET("color/{id}")
    fun getColorById(@Path("id") id: Int): Call<Color>
}
