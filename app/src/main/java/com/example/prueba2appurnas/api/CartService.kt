package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CartService {

    // ===================== CART =====================

    @POST("cart")
    suspend fun createCart(
        @Body request: CreateCartRequest
    ): Response<Cart>

    @GET("cart/{cart_id}")
    suspend fun getCartById(
        @Path("cart_id") cartId: Int
    ): Response<Cart>

    @PATCH("cart/{cart_id}")
    suspend fun updateCart(
        @Path("cart_id") cartId: Int,
        @Body request: UpdateCartRequest
    ): Response<Cart>


    // ===================== CART ITEMS =====================

    @POST("cart_item")
    suspend fun addItem(
        @Body request: AddToCartRequest
    ): Response<CartItem>

    @GET("cart_item")
    suspend fun getCartItemsForCart(
        @Query("cart_id") cartId: Int
    ): Response<List<CartItem>>

    @PATCH("cart_item/{id}")
    suspend fun updateCartItem(
        @Path("id") itemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<CartItem>

    @DELETE("cart_item/{id}")
    suspend fun deleteCartItem(
        @Path("id") itemId: Int
    ): Response<Unit>
}

