package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.OrderItemRequest
import com.example.prueba2appurnas.model.OrderItemResponse
import com.example.prueba2appurnas.model.OrderRequest
import com.example.prueba2appurnas.model.OrderResponse
import com.example.prueba2appurnas.model.UpdateOrderStatusRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

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

    @GET("order")
    fun getAllOrders(): Call<List<OrderResponse>>

    // *** ESTE ES EL ÚNICO VÁLIDO ***
    @PATCH("order/{id}")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,
        @Body body: UpdateOrderStatusRequest
    ): Response<OrderResponse>
}
