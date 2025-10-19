package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val KEY_AUTH_TOKEN = "authToken" // Solo necesitamos la clave del token
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Ya no necesitamos saveAuthData, getUserName, getUserEmail, getUserRole

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    // clearToken ahora solo necesita borrar el token
    fun clearToken() {
        prefs.edit().apply{
            remove(KEY_AUTH_TOKEN)
            // Ya no necesitamos borrar name, email, role
            apply()
        }
    }

    fun isLoggedIn(): Boolean = getToken() != null
}