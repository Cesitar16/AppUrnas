// En: com.example.prueba2appurnas.api.UrnaImageService.kt

package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.UrnaImage
import com.example.prueba2appurnas.model.ImageUrl // Asegúrate que ImageUrl esté en el paquete model
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST // Importar POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UrnaImageService {
    @GET("urn_image")
    fun getImagesByUrnaId(@Query("urna_id") urnaId: Int): Call<List<UrnaImage>>

    @Multipart
    @POST("urn_image")
    fun addUrnaImageMultipart(
        @Part("urna_id") urnaId: RequestBody,      // ID de la urna como RequestBody
        @Part("alt") altText: RequestBody?,        // Texto alternativo como RequestBody (opcional)
        @Part("is_cover") isCover: RequestBody?,   // Booleano como RequestBody (opcional, "true" o "false")
        @Part("sort_order") sortOrder: RequestBody?, // Número como RequestBody (opcional)
        // La imagen enviada como MultipartBody.Part
        // El nombre "url" debe coincidir EXACTAMENTE con el input de tipo 'file storage'
        // que espera tu API POST /urn_image en Xano cuando recibe un archivo.
        @Part imageFile: MultipartBody.Part
    ): Call<UrnaImage> // Asume que devuelve la UrnaImage creada
}