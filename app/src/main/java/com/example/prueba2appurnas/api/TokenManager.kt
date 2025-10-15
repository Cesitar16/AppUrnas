package com.example.prueba2appurnas.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.prueba2appurnas.model.User
import com.example.prueba2appurnas.model.AuthResponse

class TokenManager(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("authToken", token).apply()
    }

    fun saveUser(user: User?) {
        if (user == null || user.id == -1) {
            Log.w(TAG, "Ignoring user data without a valid id")
            return
        }

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

        val name = prefs.getString("userName", null) ?: ""
        val email = prefs.getString("userEmail", null) ?: ""
        val role = prefs.getString("userRole", null)

        return User(id = id, name = name, email = email, role = role)
    }

    suspend fun persistSession(authResponse: AuthResponse) {
        val token = authResponse.authToken
        if (token.isBlank()) {
            Log.w(TAG, "Cannot persist session without a token")
            return
        }

        saveToken(token)

        val responseUser = authResponse.user
        if (responseUser != null && responseUser.id != -1) {
            saveUser(responseUser)
            return
        }

        try {
            val userResponse = RetrofitClient.getAuthenticatedAuthService(appContext).getUser()
            if (userResponse.isSuccessful) {
                saveUser(userResponse.body())
            } else {
                Log.w(TAG, "Unable to fetch user profile. Code: ${'$'}{userResponse.code()}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching user profile", e)
        }
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

    companion object {
        private const val TAG = "TokenManager"
    }
}
