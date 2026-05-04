package com.smarthas.data.preferences

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "smarthas_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun saveUser(email: String, fullName: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, fullName)
        }.apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}
