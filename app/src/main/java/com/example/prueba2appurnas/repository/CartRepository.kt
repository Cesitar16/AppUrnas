package com.example.prueba2appurnas.repository

import com.example.prueba2appurnas.api.CartService
import com.example.prueba2appurnas.api.OrderService
import com.example.prueba2appurnas.model.AddToCartRequest
import com.example.prueba2appurnas.model.CreateCartRequest
import com.example.prueba2appurnas.model.OrderItemRequest
import com.example.prueba2appurnas.model.OrderRequest

class CartRepository(
    private val service: CartService,
    private val orderService: OrderService,
    private val localStore: CartLocalStorage
) {

    // Obtener o crear carrito
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

    // Agregar un item
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

    // Obtener items
    suspend fun getItems(cartId: Int) =
        service.getCartItemsForCart(cartId)

    // Crear orden
    suspend fun createOrder(request: OrderRequest) =
        orderService.createOrder(request)

    // Crear items de orden
    suspend fun createOrderItem(request: OrderItemRequest) =
        orderService.createOrderItem(request)
}
