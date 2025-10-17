package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "MyAppPrefs"
        private const val KEY_AUTH_TOKEN = "authToken"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }
}
