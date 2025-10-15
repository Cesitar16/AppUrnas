package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.Urna
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.GET

interface UrnaService {
    @GET("urn")
    fun getUrnas(): Call<List<Urna>>

    @GET("urnas/{id}")
    fun getUrnaById(@Path("id") id: Int): Call<Urna>

    @DELETE("urn/{urn_id}")
    fun deleteUrna(@Path("urn_id") id: Int): Call<Void>
}
