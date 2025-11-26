package com.example.prueba2appurnas.repository

import android.content.Context

class CartLocalStorage(context: Context) {

    private val prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)

    fun saveCartId(id: Int) {
        prefs.edit().putInt("cart_id", id).apply()
    }

    fun getCartId(): Int? {
        val id = prefs.getInt("cart_id", -1)
        return if (id == -1) null else id
    }
}
