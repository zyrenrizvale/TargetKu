package com.rizki.targetku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rizki.targetku.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val isFirstTime: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _usernameInput = MutableStateFlow("")
    val usernameInput: StateFlow<String> = _usernameInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    fun onUsernameChange(value: String) {
        _usernameInput.value = value
        if (_authState.value is AuthState.Error) _authState.value = AuthState.Idle
    }

    fun onPasswordChange(value: String) {
        _passwordInput.value = value
        if (_authState.value is AuthState.Error) _authState.value = AuthState.Idle
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun login() {
        _authState.value = AuthState.Loading

        val username = _usernameInput.value.trim()
        val password = _passwordInput.value.trim()

        if (username == DEMO_USERNAME && password == DEMO_PASSWORD) {
            prefsManager.isLoggedIn = true
            val isFirstTime = !prefsManager.isOnboarded
            _authState.value = AuthState.Success(isFirstTime = isFirstTime)
        } else {
            _authState.value = AuthState.Error("Username atau password salah. Coba lagi!")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        private const val DEMO_USERNAME = "rizkibismillahsnu"
        private const val DEMO_PASSWORD = "130310"
    }
}
