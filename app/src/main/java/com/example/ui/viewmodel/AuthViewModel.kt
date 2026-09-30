package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.config.AppConfig
import com.example.data.remote.AuthResult
import com.example.data.remote.FirebaseAuthService
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authService: FirebaseAuthService = FirebaseAuthService()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _resetPasswordMessage = MutableStateFlow<String?>(null)
    val resetPasswordMessage: StateFlow<String?> = _resetPasswordMessage.asStateFlow()

    init {
        checkInitialAuth()
    }

    private fun checkInitialAuth() {
        viewModelScope.launch {
            val fbUser = authService.currentUser
            if (fbUser != null) {
                val profile = authService.getUserProfile(fbUser.uid)
                if (profile != null) {
                    _currentUser.value = profile
                    _uiState.value = AuthUiState.Authenticated(profile)
                }
            }
        }
    }

    fun register(name: String, email: String, pass: String, confirmPass: String) {
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("كلمة المرور وتأكيدها غير متطابقين")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authService.registerWithEmail(name, email, pass)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = AuthUiState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authService.loginWithEmail(email, pass)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = AuthUiState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authService.signInWithGoogle(context)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data
                    _uiState.value = AuthUiState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authService.sendPasswordReset(email)) {
                is AuthResult.Success -> {
                    _resetPasswordMessage.value = "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني"
                    _uiState.value = AuthUiState.Idle
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                is AuthResult.Loading -> {}
            }
        }
    }

    fun clearResetMessage() {
        _resetPasswordMessage.value = null
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            authService.signOut(context)
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
        }
    }
}
