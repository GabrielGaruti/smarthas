package com.smarthas.data.repository

import com.smarthas.data.api.SmartHasApi
import com.smarthas.data.api.LoginRequest
import com.smarthas.data.api.RegisterRequest
import com.smarthas.data.preferences.TokenManager

class AuthRepository(
    private val api: SmartHasApi,
    private val tokenManager: TokenManager
) {
    suspend fun register(email: String, fullName: String, password: String) {
        val request = RegisterRequest(email, fullName, password)
        val response = api.register(request)
    }

    suspend fun login(email: String, password: String): Boolean {
        try {
            val request = LoginRequest(email, password)
            val response = api.login(request)
            tokenManager.saveToken(response.token)
            tokenManager.saveUser(response.user.email, response.user.fullName)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun logout() {
        tokenManager.logout()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getToken(): String? {
        return tokenManager.getToken()
    }

    fun getUserEmail(): String? {
        return tokenManager.getUserEmail()
    }

    fun getUserName(): String? {
        return tokenManager.getUserName()
    }
}
