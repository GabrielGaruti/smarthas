package com.smarthas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthas.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser

    init {
        if (authRepository.isLoggedIn()) {
            _currentUser.value = CurrentUser(
                email = authRepository.getUserEmail() ?: "",
                fullName = authRepository.getUserName() ?: ""
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val success = authRepository.login(email, password)
                if (success) {
                    _currentUser.value = CurrentUser(
                        email = authRepository.getUserEmail() ?: "",
                        fullName = authRepository.getUserName() ?: ""
                    )
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("Email ou senha inválidos")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun register(email: String, fullName: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                authRepository.register(email, fullName, password)
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Erro ao criar conta")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _loginState.value = LoginState.Idle
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

data class CurrentUser(
    val email: String,
    val fullName: String
)
