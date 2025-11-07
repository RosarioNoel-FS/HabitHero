package com.example.habithero

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChallengeDetailUiState(
    val isLoading: Boolean = false,
    val challenge: Challenge? = null,
    val error: String? = null,
    val challengeAccepted: Boolean = false
)

class ChallengeDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val challengeId: String = checkNotNull(savedStateHandle["challengeId"])

    private val _uiState = MutableStateFlow(ChallengeDetailUiState(isLoading = true))
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    init {
        loadChallenge()
    }

    private fun loadChallenge() {
        viewModelScope.launch {
            val challenge = ChallengeData.challenges.find { it.id == challengeId }
            if (challenge != null) {
                _uiState.value = ChallengeDetailUiState(challenge = challenge)
            } else {
                _uiState.value = ChallengeDetailUiState(error = "Challenge not found.")
            }
        }
    }

    fun acceptChallenge() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val challenge = _uiState.value.challenge ?: return@launch

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val batch = db.batch()
                val habitsCollection = db.collection("users").document(userId).collection("habits")

                challenge.habits.forEach { habitTemplate ->
                    val newHabitRef = habitsCollection.document()
                    // Correctly create the Habit object based on its constructor
                    val habit = Habit(
                        name = habitTemplate.name,
                        category = habitTemplate.category,
                        emoji = habitTemplate.emoji,
                        completionHour = habitTemplate.completionHour,
                        completionMinute = habitTemplate.completionMinute,
                        iconUrl = habitTemplate.iconUrl,
                        completionCount = 0,
                        completionDates = emptyList()
                    ).apply {
                        // Set the id separately, as it's not in the constructor
                        id = newHabitRef.id
                    }
                    batch.set(newHabitRef, habit)
                }

                batch.commit().await()
                _uiState.value = _uiState.value.copy(isLoading = false, challengeAccepted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
