package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.UrnaImage
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UrnaImageService {
    @GET("urn_image")
    fun getImagesByUrnaId(@Query("urna_id") urnaId: Int): Call<List<UrnaImage>>
}
