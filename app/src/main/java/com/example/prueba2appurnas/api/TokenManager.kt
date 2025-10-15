package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences
import com.example.prueba2appurnas.model.User

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("authToken", token).apply()
    }

    fun saveUser(user: User) {
        prefs.edit()
            .putInt("userId", user.id)
            .putString("userName", user.name)
            .putString("userEmail", user.email)
            .putString("userRole", user.role)
            .apply()
    }

    fun getToken(): String? {
        return prefs.getString("authToken", null)
    }

    fun getUser(): User? {
        val id = prefs.getInt("userId", -1)
        if (id == -1) return null

        val name = prefs.getString("userName", null) ?: return null
        val email = prefs.getString("userEmail", null) ?: return null
        val role = prefs.getString("userRole", null) ?: return null

        return User(id, name, email, role)
    }

    fun clearToken() {
        prefs.edit()
            .remove("authToken")
            .remove("userId")
            .remove("userName")
            .remove("userEmail")
            .remove("userRole")
            .apply()
    }
}
