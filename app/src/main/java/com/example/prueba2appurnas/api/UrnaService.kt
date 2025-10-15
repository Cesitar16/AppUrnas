package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.Urna
import retrofit2.Call
import retrofit2.http.GET

interface UrnaService {
    @GET("urn")
    fun getUrnas(): Call<List<Urna>>
}
