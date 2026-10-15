package com.rutapay.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rutapay.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val message: String = "") : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Ingresa correo y contraseña")
            return
        }
        _state.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = {
                    _state.value = AuthState.Success("Bienvenido")
                    onSuccess()
                },
                onFailure = { e ->
                    _state.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
                }
            )
        }
    }

    fun register(
        fullName: String,
        lastNames: String,
        email: String,
        password: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        if (fullName.isBlank() || lastNames.isBlank() || email.isBlank() || password.isBlank() || phoneNumber.isBlank()) {
            _state.value = AuthState.Error("Completa todos los campos")
            return
        }
        _state.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.register(fullName, lastNames, email, password, phoneNumber)
            result.fold(
                onSuccess = {
                    _state.value = AuthState.Success("Cuenta creada. Ahora inicia sesión.")
                    onSuccess()
                },
                onFailure = { e ->
                    _state.value = AuthState.Error(e.message ?: "Error al registrarse")
                }
            )
        }
    }

    fun reset() {
        _state.value = AuthState.Idle
    }

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
