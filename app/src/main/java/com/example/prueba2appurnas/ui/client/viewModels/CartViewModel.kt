package com.example.prueba2appurnas.ui.client.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.prueba2appurnas.api.RetrofitClient
import com.example.prueba2appurnas.model.CartItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loadCart(context: Context) {
        _isLoading.value = true

        val cartService = RetrofitClient.getCartService(context)

        cartService.getCartItems().enqueue(object : Callback<List<CartItem>> {
            override fun onResponse(
                call: Call<List<CartItem>>,
                response: Response<List<CartItem>>
            ) {
                _isLoading.value = false

                if (response.isSuccessful) {
                    _cartItems.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Error obteniendo carrito"
                }
            }

            override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = t.localizedMessage
            }
        })
    }
}
