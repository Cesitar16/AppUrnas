package com.example.prueba2appurnas.repository

import com.example.prueba2appurnas.api.CartService
import com.example.prueba2appurnas.api.OrderService
import com.example.prueba2appurnas.model.AddToCartRequest
import com.example.prueba2appurnas.model.CreateCartRequest
import com.example.prueba2appurnas.model.OrderItemRequest
import com.example.prueba2appurnas.model.OrderRequest
import com.example.prueba2appurnas.model.UpdateOrderStatusRequest

class CartRepository(
    private val service: CartService,
    private val orderService: OrderService,
    private val localStore: CartLocalStorage
) {

    suspend fun getOrCreateCart(userId: Int): Int? {

        val savedId = localStore.getCartId()
        if (savedId != null) {
            return savedId
        }

        val response = service.createCart(
            CreateCartRequest(
                status = "OPEN",
                updated_at = System.currentTimeMillis(),
                user_id = userId
            )
        )

        if (!response.isSuccessful) return null

        val cartId = response.body()?.id ?: return null

        localStore.saveCartId(cartId)

        return cartId
    }

    suspend fun addItem(cartId: Int, urnId: Int, price: Double) =
        service.addItem(
            AddToCartRequest(
                quantity = 1,
                unit_price = price,
                updated_at = System.currentTimeMillis(),
                cart_id = cartId,
                urn_id = urnId
            )
        )

    suspend fun getItems(cartId: Int) =
        service.getCartItemsForCart(cartId)

    suspend fun createOrder(request: OrderRequest) =
        orderService.createOrder(request)

    suspend fun createOrderItem(request: OrderItemRequest) =
        orderService.createOrderItem(request)

    // FIX: AHORA ENVÍA UN OBJETO, NO UN MAP
    suspend fun updateOrderStatus(id: Int, status: String) =
        orderService.updateOrderStatus(
            id,
            UpdateOrderStatusRequest(
                status = status,
                updated_at = System.currentTimeMillis()
            )
        )
}
