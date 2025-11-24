package com.example.habithero

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.model.Challenge
import com.example.habithero.model.ChallengeEnrollment
import com.example.habithero.model.Habit
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
    val challengeAccepted: Boolean = false,
    val isAlreadyAccepted: Boolean = false,
    val missingHabitIds: List<String> = emptyList()
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

            try {
                // **THE FIX: Fetch the specific challenge document from Firestore**
                val challengeDoc = db.collection("challenges").document(challengeId).get().await()
                val challenge = challengeDoc.toObject(Challenge::class.java)?.copy(id = challengeDoc.id)

                if (challenge == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Challenge not found.")
                    return@launch
                }

                // The rest of the logic for checking enrollment status remains the same.
                val enrollmentRef = db.collection("users").document(userId).collection("challengeEnrollments").document(challengeId)
                val enrollment = enrollmentRef.get().await().toObject(ChallengeEnrollment::class.java)

                if (enrollment != null) {
                    // User is enrolled, check for missing habits
                    val userHabits = db.collection("users").document(userId).collection("habits")
                        .whereEqualTo("sourceChallengeId", challengeId)
                        .get()
                        .await()
                        .toObjects(Habit::class.java)

                    val existingTemplateIds = userHabits.map { it.sourceTemplateId }.toSet()
                    val missingTemplates = challenge.habits.filter { !existingTemplateIds.contains(it.id) }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        challenge = challenge,
                        isAlreadyAccepted = true,
                        missingHabitIds = missingTemplates.map { it.id }
                    )
                } else {
                    // User is not enrolled
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        challenge = challenge,
                        isAlreadyAccepted = false
                    )
                }

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
                db.runTransaction { transaction ->
                    val enrollmentRef = db.collection("users").document(userId).collection("challengeEnrollments").document(challengeId)
                    transaction.set(enrollmentRef, ChallengeEnrollment(challengeId = challengeId))

                    challenge.habits.forEach { habitTemplate ->
                        val deterministicId = "$userId:$challengeId:${habitTemplate.id}"
                        val habitRef = db.collection("users").document(userId).collection("habits").document(deterministicId)

                        val habit = Habit(
                            name = habitTemplate.name,
                            category = habitTemplate.category,
                            emoji = habitTemplate.emoji,
                            completionHour = habitTemplate.completionHour,
                            completionMinute = habitTemplate.completionMinute,
                            iconUrl = habitTemplate.iconUrl,
                            sourceChallengeId = challengeId,
                            sourceTemplateId = habitTemplate.id
                        ).apply {
                            id = deterministicId
                        }
                        transaction.set(habitRef, habit)
                    }
                    null
                }.await()

                _uiState.value = _uiState.value.copy(isLoading = false, challengeAccepted = true, isAlreadyAccepted = true, missingHabitIds = emptyList())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addMissingHabitsForChallenge() {
        viewModelScope.launch {
             if (userId == null) return@launch
            val challenge = _uiState.value.challenge ?: return@launch
            val missingIds = _uiState.value.missingHabitIds
            if (missingIds.isEmpty()) return@launch

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val habitsToCreate = challenge.habits.filter { missingIds.contains(it.id) }

                db.runBatch { batch ->
                     habitsToCreate.forEach { habitTemplate ->
                        val deterministicId = "$userId:$challengeId:${habitTemplate.id}"
                        val habitRef = db.collection("users").document(userId).collection("habits").document(deterministicId)
                        val habit = Habit(
                            name = habitTemplate.name,
                            category = habitTemplate.category,
                            emoji = habitTemplate.emoji,
                            completionHour = habitTemplate.completionHour,
                            completionMinute = habitTemplate.completionMinute,
                            iconUrl = habitTemplate.iconUrl,
                            sourceChallengeId = challengeId,
                            sourceTemplateId = habitTemplate.id
                        ).apply{
                            id = deterministicId
                        }
                        batch.set(habitRef, habit)
                    }
                }.await()
                _uiState.value = _uiState.value.copy(isLoading = false, missingHabitIds = emptyList())

            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
