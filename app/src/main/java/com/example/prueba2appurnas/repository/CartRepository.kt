package com.example.prueba2appurnas.repository

import com.example.prueba2appurnas.api.CartService
import com.example.prueba2appurnas.model.AddToCartRequest
import com.example.prueba2appurnas.model.CreateCartRequest

class CartRepository(
    private val service: CartService,
    private val localStore: CartLocalStorage
) {

    // Obtener o crear carrito del usuario
    suspend fun getOrCreateCart(userId: Int): Int? {

        // 1. Revisar si ya tengo el cart guardado en memoria local
        val savedId = localStore.getCartId()
        if (savedId != null) {
            return savedId
        }

        // 2. Crear carrito nuevo en Xano
        val response = service.createCart(
            CreateCartRequest(
                status = "OPEN",
                updated_at = System.currentTimeMillis(),
                user_id = userId
            )
        )

        if (!response.isSuccessful) return null

        val cartId = response.body()?.id ?: return null

        // Guardar el nuevo carrito
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

    // Obtener items del carrito
    suspend fun getItems(cartId: Int) =
        service.getCartItemsForCart(cartId)
}
