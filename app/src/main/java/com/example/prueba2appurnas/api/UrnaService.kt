package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.Urna
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.* // Importa todas las anotaciones HTTP

interface UrnaService {

    /**
     * Obtiene la lista completa de urnas.
     * Endpoint: GET /urn
     */
    @GET("urn")
    fun getUrnas(): Call<List<Urna>>

    /**
     * Obtiene los detalles de una urna específica por su ID.
     * Endpoint: GET /urn/{id}
     */
    @GET("urn/{id}")
    fun getUrna(@Path("id") id: Int): Call<Urna>

    /**
     * Actualiza una urna existente.
     * Endpoint: PATCH /urn/{id} (o PUT si tu API usa PUT)
     * Usamos un Map para enviar solo los campos modificados.
     */
    @PATCH("urn/{id}") // O @PUT si es necesario
    fun updateUrna(@Path("id") id: Int, @Body urnaData: Map<String, @JvmSuppressWildcards Any>): Call<Urna>

    /**
     * Crea una nueva urna enviando todos los datos como multipart/form-data.
     * La imagen se envía como un archivo binario directamente.
     * Endpoint: POST /urn
     */
    @Multipart
    @POST("urn")
    fun createUrnaMultipart(
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody,
        @Part("available") available: RequestBody,
        @Part("short_description") shortDescription: RequestBody?, // Opcional
        @Part("detailed_description") detailedDescription: RequestBody?, // Opcional
        @Part imageFile: MultipartBody.Part // La imagen principal (el nombre "image_url" se define al crear la Part)
        // Añade más @Part si necesitas enviar otros campos obligatorios u opcionales
        // Ejemplo: @Part("color_id") colorId: RequestBody?,
    ): Call<Urna> // Asumimos que Xano devuelve la urna creada

    /**
     * Elimina una urna por su ID.
     * Endpoint: DELETE /urn/{id}
     */
    @DELETE("urn/{id}")
    fun deleteUrna(@Path("id") id: Int): Call<Void> // O Call<Response<Void>> si quieres info de la respuesta

    /*
    // Método antiguo para subir imagen en Edit, revisar si aún es necesario o se maneja diferente.
    // Podría ser reemplazado por una lógica similar a createUrnaMultipart si editas la imagen.
    @Multipart
    @POST("upload/image") // Endpoint genérico de subida
    fun uploadUrnaImage(@Part image: MultipartBody.Part): Call<Urna> // Revisar el tipo de retorno esperado
    */

}