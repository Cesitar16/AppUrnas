package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val KEY_AUTH_TOKEN = "authToken"
        private const val KEY_USER_EMAIL = "userEmail"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ===============================
    // TOKEN
    // ===============================
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun isLoggedIn(): Boolean = getToken() != null

    // ===============================
    // EMAIL
    // ===============================
    fun saveUserEmail(email: String?) {
        prefs.edit().putString(KEY_USER_EMAIL, email ?: "").apply()
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "Correo no disponible") ?: "Correo no disponible"
    }

    // ===============================
    // LOGOUT / LIMPIAR TODO
    // ===============================
    fun clear() {
        prefs.edit().clear().apply()
    }
}
