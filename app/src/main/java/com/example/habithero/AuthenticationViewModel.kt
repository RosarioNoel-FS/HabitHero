package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private val mAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthenticationState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun toggleAuthenticationMode() {
        val newMode = if (_uiState.value.authenticationMode == AuthenticationMode.SIGN_IN) {
            AuthenticationMode.SIGN_UP
        } else {
            AuthenticationMode.SIGN_IN
        }
        _uiState.value = _uiState.value.copy(
            authenticationMode = newMode,
            error = null // Clear errors when switching modes
        )
    }

    fun authenticate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val email = _uiState.value.email
            val password = _uiState.value.password

            if (email.isBlank() || password.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Email and password cannot be empty.")
                return@launch
            }

            if (_uiState.value.authenticationMode == AuthenticationMode.SIGN_UP) {
                if (password != _uiState.value.confirmPassword) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Passwords do not match.")
                    return@launch
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = task.exception?.message)
                        }
                    }
            } else { // SIGN_IN
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = task.exception?.message)
                        }
                    }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = task.exception?.message)
                    }
                }
        }
    }

    fun onGoogleSignInError() {
        _uiState.value = _uiState.value.copy(error = "Google Sign-In failed.")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

enum class AuthenticationMode {
    SIGN_IN,
    SIGN_UP
}

data class AuthenticationState(
    val authenticationMode: AuthenticationMode = AuthenticationMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)
