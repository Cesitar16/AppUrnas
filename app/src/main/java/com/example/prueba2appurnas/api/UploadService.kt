package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.ImageUrl // Importa tu modelo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {

    /**
     * Sube un archivo de imagen al endpoint genérico de Xano.
     * Devuelve el objeto ImageUrl con los metadatos de la imagen almacenada.
     * Endpoint: POST /upload/image (Estándar de Xano)
     */
    @Multipart
    @POST("upload/image")
    fun uploadImage(@Part image: MultipartBody.Part): Call<ImageUrl>
}