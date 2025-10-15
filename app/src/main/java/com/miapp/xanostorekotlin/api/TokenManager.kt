package com.miapp.xanostorekotlin.api

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.miapp.xanostorekotlin.model.AuthResponse
import com.miapp.xanostorekotlin.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun saveAuth(response: AuthResponse) = withContext(Dispatchers.IO) {
        prefs.edit {
            putString(KEY_TOKEN, response.authToken)
            response.user?.let { user ->
                putString(KEY_USER_ID, user.id)
                putString(KEY_USER_NAME, user.name)
                putString(KEY_USER_EMAIL, user.email)
                putString(KEY_USER_CREATED_AT, user.createdAt ?: "")
            }
            putLong(KEY_SAVED_AT, System.currentTimeMillis())
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val createdAt = prefs.getString(KEY_USER_CREATED_AT, null)
        return User(id = id, name = name, email = email, createdAt = createdAt)
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clear() {
        prefs.edit { clear() }
    }

    fun hasExpired(ttlSeconds: Int): Boolean {
        val savedAt = prefs.getLong(KEY_SAVED_AT, 0L)
        if (savedAt == 0L) return true
        val elapsedSec = (System.currentTimeMillis() - savedAt) / 1000L
        return elapsedSec >= ttlSeconds
    }

    companion object {
        private const val PREFS_NAME = "xano_store_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_CREATED_AT = "user_created_at"
        private const val KEY_SAVED_AT = "token_saved_at"
    }
}
