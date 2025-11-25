package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.FirebaseHelper
import com.example.habithero.model.UserStats
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RewardsUiState(
    val userStats: UserStats = UserStats(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class RewardsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseHelper = FirebaseHelper()

    init {
        loadUserStats()
    }

    private fun loadUserStats() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in", isLoading = false) }
            return
        }

        viewModelScope.launch {
            try {
                val stats = firebaseHelper.getUserStats(userId)
                _uiState.update { it.copy(userStats = stats ?: UserStats(), isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load stats: ${e.message}", isLoading = false) }
            }
        }
    }
}
