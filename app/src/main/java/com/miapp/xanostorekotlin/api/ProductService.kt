package com.miapp.xanostorekotlin.api

import com.miapp.xanostorekotlin.model.CreateProductRequest
import com.miapp.xanostorekotlin.model.CreateProductResponse
import com.miapp.xanostorekotlin.model.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProductService {
    @GET("product")
    suspend fun getProducts(): List<Product>

    @POST("product")
    suspend fun createProduct(@Body request: CreateProductRequest): CreateProductResponse
}
