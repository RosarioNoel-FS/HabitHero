package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChallengesUiState(
    val acceptedChallengeIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ChallengesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        fetchAcceptedChallenges()
    }

    private fun fetchAcceptedChallenges() {
        viewModelScope.launch {
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in.")
                return@launch
            }

            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val acceptedIds = userDoc.get("acceptedChallenges") as? List<String> ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    acceptedChallengeIds = acceptedIds.toSet()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
