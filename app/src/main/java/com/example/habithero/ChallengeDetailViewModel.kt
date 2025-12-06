package com.example.habithero

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.model.Challenge
import com.example.habithero.model.ChallengeEnrollment
import com.example.habithero.model.Habit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class ChallengeDetailUiState(
    val isLoading: Boolean = false,
    val challenge: Challenge? = null,
    val error: String? = null,
    val challengeAccepted: Boolean = false, // Re-added for one-time navigation event
    val isAlreadyAccepted: Boolean = false,
    val missingHabitIds: List<String> = emptyList()
)

class ChallengeDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val challengeId: String = checkNotNull(savedStateHandle["challengeId"])
    private val userId = auth.currentUser?.uid
    private var enrollmentListener: ListenerRegistration? = null
    private var habitsListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(ChallengeDetailUiState(isLoading = true))
    val uiState: StateFlow<ChallengeDetailUiState> = _uiState.asStateFlow()

    init {
        loadChallengeAndSubscribeToUpdates()
    }

    private fun loadChallengeAndSubscribeToUpdates() {
        viewModelScope.launch {
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in.")
                return@launch
            }

            try {
                val challengeDoc = db.collection("challenges").document(challengeId).get().await()
                val challenge = challengeDoc.toObject(Challenge::class.java)?.copy(id = challengeDoc.id)

                if (challenge == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Challenge not found.")
                    return@launch
                }

                // Listen to enrollment status first
                enrollmentListener?.remove()
                enrollmentListener = db.collection("users").document(userId)
                    .collection("challengeEnrollments").document(challengeId)
                    .addSnapshotListener { enrollmentSnapshot, error ->
                        if (error != null) {
                            _uiState.value = _uiState.value.copy(error = "Failed to load enrollment status.")
                            return@addSnapshotListener
                        }

                        val enrollment = enrollmentSnapshot?.toObject(ChallengeEnrollment::class.java)
                        val isAccepted = enrollment != null

                        _uiState.update { it.copy(isAlreadyAccepted = isAccepted) }

                        if (isAccepted) {
                            listenToHabitUpdates(userId, challenge, enrollment)
                        } else {
                            habitsListener?.remove()
                            _uiState.update { it.copy(isLoading = false, challenge = challenge) }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun listenToHabitUpdates(userId: String, challenge: Challenge, enrollment: ChallengeEnrollment?) {
        habitsListener?.remove()
        habitsListener = db.collection("users").document(userId).collection("habits")
            .whereEqualTo("sourceChallengeId", challengeId)
            .addSnapshotListener { habitsSnapshot, error ->
                if (error != null || habitsSnapshot == null) {
                    _uiState.value = _uiState.value.copy(error = "Failed to load habits.")
                    return@addSnapshotListener
                }

                val userHabits = habitsSnapshot.toObjects(Habit::class.java)
                val allCompleted = userHabits.isNotEmpty() && userHabits.all { habit ->
                    habit.lastCompletionDate?.let {
                        org.threeten.bp.Instant.ofEpochMilli(it.time)
                            .atZone(org.threeten.bp.ZoneId.systemDefault()).toLocalDate() == org.threeten.bp.LocalDate.now()
                    } ?: false
                }

                val existingTemplateIds = userHabits.map { it.sourceTemplateId }.toSet()
                val missingTemplates = challenge.habits.filter { !existingTemplateIds.contains(it.id) }
                
                val currentDay = if (enrollment?.startDate != null) {
                    val diff = System.currentTimeMillis() - enrollment.startDate!!.time
                    (diff / (1000 * 60 * 60 * 24)).toInt() + 1
                } else {
                    1
                }

                val updatedChallenge = challenge.copy(
                    isCompletedToday = allCompleted,
                    currentDay = currentDay,
                    daysTotal = challenge.durationDays,
                    progressPercent = if (challenge.durationDays > 0) currentDay.toFloat() / challenge.durationDays else 0f,
                    lives = enrollment?.lives ?: 3
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        challenge = updatedChallenge,
                        missingHabitIds = missingTemplates.map { it.id }
                    )
                }
            }
    }

    fun acceptChallenge() {
        viewModelScope.launch {
            if (userId == null) return@launch
            val challenge = _uiState.value.challenge ?: return@launch

            _uiState.update { it.copy(isLoading = true) }
            try {
                db.runTransaction { transaction ->
                    val enrollmentRef = db.collection("users").document(userId).collection("challengeEnrollments").document(challengeId)
                    transaction.set(enrollmentRef, ChallengeEnrollment(challengeId = challengeId, startDate = Date()))
                    
                    val statsRef = db.collection("users").document(userId).collection("stats").document("user_stats")
                    transaction.update(statsRef, "activeHabits", FieldValue.increment(challenge.habits.size.toLong()))

                    challenge.habits.forEach { habitTemplate ->
                        val deterministicId = "$userId:$challengeId:${habitTemplate.id}"
                        val habitRef = db.collection("users").document(userId).collection("habits").document(deterministicId)
                        val newHabit = Habit(
                            name = habitTemplate.name,
                            category = habitTemplate.category,
                            emoji = habitTemplate.emoji,
                            completionHour = habitTemplate.completionHour,
                            completionMinute = habitTemplate.completionMinute,
                            iconUrl = habitTemplate.iconUrl,
                            sourceChallengeId = challengeId,
                            sourceTemplateId = habitTemplate.id
                        ).apply { id = deterministicId }
                        transaction.set(habitRef, newHabit)
                    }
                }.await()

                _uiState.update { it.copy(isLoading = false, challengeAccepted = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onChallengeAcceptedNavigated() {
        _uiState.update { it.copy(challengeAccepted = false) }
    }

    fun addMissingHabitsForChallenge() {
        viewModelScope.launch {
             if (userId == null) return@launch
            val challenge = _uiState.value.challenge ?: return@launch
            val missingIds = _uiState.value.missingHabitIds
            if (missingIds.isEmpty()) return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                val habitsToCreate = challenge.habits.filter { missingIds.contains(it.id) }

                db.runBatch { batch ->
                    val statsRef = db.collection("users").document(userId).collection("stats").document("user_stats")
                    batch.update(statsRef, "activeHabits", FieldValue.increment(habitsToCreate.size.toLong()))

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
                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                 _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        habitsListener?.remove()
        enrollmentListener?.remove()
    }
}
