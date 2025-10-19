package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val KEY_AUTH_TOKEN = "authToken"

        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_ROLE = "userRole"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- FUNCIÓN MODIFICADA PARA GUARDAR TODO ---
    fun saveAuthData(token: String, name: String?, email: String?, role: String?) {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply() // Guardar los cambios
        }
    }

    // --- NUEVAS FUNCIONES GETTER ---
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }


    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    // Función para borrar todo al cerrar sesión
    fun clearToken() {
        prefs.edit().apply{
            remove(KEY_AUTH_TOKEN)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_ROLE)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
