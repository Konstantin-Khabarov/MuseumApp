package com.example.museumapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museumapp.data.auth.AuthManager
import com.example.museumapp.data.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Checking)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {

        viewModelScope.launch {
            AuthManager.authState.collect { authState ->
                _uiState.value = when (authState) {
                    AuthState.Checking -> AuthUiState.Checking
                    AuthState.Authenticated -> AuthUiState.Authenticated
                    AuthState.Unauthenticated -> AuthUiState.Unauthenticated
                }
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return AuthManager.logout()
    }
}

sealed class AuthUiState {
    object Checking : AuthUiState()
    object Authenticated : AuthUiState()
    object Unauthenticated : AuthUiState()
}
