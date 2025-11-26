package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.OrderRequest
import com.example.prueba2appurnas.model.OrderResponse
import com.example.prueba2appurnas.model.OrderItemRequest
import com.example.prueba2appurnas.model.OrderItemResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderService {

    @POST("order")
    suspend fun createOrder(
        @Body body: OrderRequest
    ): Response<OrderResponse>

    @POST("order_item")
    suspend fun createOrderItem(
        @Body body: OrderItemRequest
    ): Response<OrderItemResponse>

    @GET("order/{id}")
    suspend fun getOrder(
        @Path("id") id: Int
    ): Response<OrderResponse>
}
