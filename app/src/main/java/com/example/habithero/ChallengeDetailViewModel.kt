package com.example.habithero

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
    val challengeAccepted: Boolean = false,
    val isAlreadyAccepted: Boolean = false
)

class ChallengeDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val challengeId: String = checkNotNull(savedStateHandle["challengeId"])
    private val userId = auth.currentUser?.uid

    private val _uiState = MutableStateFlow(ChallengeDetailUiState(isLoading = true))
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    init {
        loadChallengeAndCheckStatus()
    }

    private fun loadChallengeAndCheckStatus() {
        viewModelScope.launch {
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in.")
                return@launch
            }
            
            val challenge = ChallengeData.challenges.find { it.id == challengeId }
            if (challenge == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Challenge not found.")
                return@launch
            }

            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val acceptedChallenges = userDoc.get("acceptedChallenges") as? List<String> ?: emptyList()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    challenge = challenge,
                    isAlreadyAccepted = acceptedChallenges.contains(challengeId)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun acceptChallenge() {
        viewModelScope.launch {
            if (userId == null) return@launch
            val challenge = _uiState.value.challenge ?: return@launch

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                db.runBatch { batch ->
                    val habitsCollection = db.collection("users").document(userId).collection("habits")
                    challenge.habits.forEach { habitTemplate ->
                        val newHabitRef = habitsCollection.document()
                        val habit = Habit(
                            name = habitTemplate.name,
                            category = habitTemplate.category,
                            emoji = habitTemplate.emoji,
                            completionHour = habitTemplate.completionHour,
                            completionMinute = habitTemplate.completionMinute,
                            iconUrl = habitTemplate.iconUrl,
                            completionCount = 0,
                            completionDates = emptyList(),
                            reminderEnabled = false,
                            reminderTimeMinutes = 15 
                        ).apply {
                            id = newHabitRef.id
                        }
                        batch.set(newHabitRef, habit)
                    }
                    
                    val userDocRef = db.collection("users").document(userId)
                    batch.update(userDocRef, "acceptedChallenges", FieldValue.arrayUnion(challenge.id))

                }.await()
                _uiState.value = _uiState.value.copy(isLoading = false, challengeAccepted = true, isAlreadyAccepted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
