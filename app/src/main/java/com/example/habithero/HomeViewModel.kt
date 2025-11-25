package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.FirebaseHelper
import com.example.habithero.model.Habit
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "Hero",
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val firebaseHelper = FirebaseHelper()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadContent() // Load content initially
    }

    fun loadContent() { // Now public to allow refreshing
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val currentUser = auth.currentUser
            val userName = currentUser?.displayName ?: "Hero"
            
            _uiState.update { it.copy(userName = userName) }

            val userId = currentUser?.uid
            if (userId != null) {
                try {
                    firebaseHelper.initializeUserStats(userId) // Initialize stats
                    val habits = firebaseHelper.fetchUserHabitsSuspend(userId)
                    _uiState.update { it.copy(habits = habits, isLoading = false) }
                } catch (e: Exception) {
                    // Handle error, e.g., show a message to the user
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteHabit(habitId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firebaseHelper.deleteHabitSuspend(userId, habitId)
                loadContent() // Refresh the list after deletion
            } catch (e: Exception) {
                // Optionally handle the error, e.g., show a snackbar
            }
        }
    }
}
