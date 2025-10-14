package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("authToken", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("authToken", null)
    }

    fun clearToken() {
        prefs.edit().remove("authToken").apply()
    }
}
