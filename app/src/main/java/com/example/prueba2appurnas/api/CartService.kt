package com.example.prueba2appurnas.api

import com.example.prueba2appurnas.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CartService {

    // ============================================================
    // CART
    // ============================================================

    @POST("/cart")
    suspend fun createCart(
        @Body request: CreateCartRequest
    ): Response<Cart>

    @GET("/cart")
    suspend fun getAllCarts(): Response<List<Cart>>

    @GET("/cart/{cart_id}")
    suspend fun getCartById(
        @Path("cart_id") cartId: Int
    ): Response<Cart>

    @PATCH("/cart/{cart_id}")
    suspend fun updateCart(
        @Path("cart_id") cartId: Int,
        @Body request: UpdateCartRequest
    ): Response<Cart>

    @DELETE("/cart/{cart_id}")
    suspend fun deleteCart(
        @Path("cart_id") cartId: Int
    ): Response<Unit>


    // ============================================================
    // CART ITEMS
    // ============================================================

    @POST("/cart_item")
    fun addItem(
        @Body request: AddToCartRequest
    ): Call<CartItem>

    @GET("/cart_item")
    fun getCartItems(): Call<List<CartItem>>

    @GET("/cart_item/{cart_item_id}")
    suspend fun getCartItemById(
        @Path("cart_item_id") itemId: Int
    ): Response<CartItem>

    @PATCH("/cart_item/{cart_item_id}")
    suspend fun updateCartItem(
        @Path("cart_item_id") itemId: Int,
        @Body request: UpdateCartItemRequest
    ): Response<CartItem>

    @DELETE("/cart_item/{cart_item_id}")
    suspend fun deleteCartItem(
        @Path("cart_item_id") itemId: Int
    ): Response<Unit>

}
