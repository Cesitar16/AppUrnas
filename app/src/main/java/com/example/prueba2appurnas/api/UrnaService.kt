package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.ImageUrl
import com.example.prueba2appurnas.model.Urna
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface UrnaService {
    @GET("urn")
    fun getUrnas(): Call<List<Urna>>

    @PATCH("urn/{urn_id}")
    fun updateUrna(
        @Path("urn_id") urnId: Int,
        @Body urna: Urna
    ): Call<Urna>

    @Multipart
    @POST("urn_image")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ImageUrl>
}
