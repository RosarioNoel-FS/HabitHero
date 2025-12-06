package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.model.Challenge
import com.example.habithero.model.ChallengeEnrollment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val acceptedChallengeIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ChallengesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var habitsListener: ListenerRegistration? = null
    private var enrollmentsListener: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                subscribeToUpdates(user.uid)
            } else {
                habitsListener?.remove()
                enrollmentsListener?.remove()
                _uiState.update {
                    it.copy(
                        challenges = emptyList(),
                        acceptedChallengeIds = emptySet(),
                        isLoading = false,
                        error = "You must be logged in to view challenges."
                    )
                }
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    private fun subscribeToUpdates(userId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val recalculateState = { 
            viewModelScope.launch {
                try {
                    val allChallenges = db.collection("challenges").get().await()
                        .documents.mapNotNull { doc -> doc.toObject(Challenge::class.java)?.copy(id = doc.id) }

                    val enrollments = db.collection("users").document(userId)
                        .collection("challengeEnrollments").get().await()
                    val userHabits = db.collection("users").document(userId).collection("habits").get().await().documents

                    val acceptedIds = enrollments.documents.map { it.id }.toSet()

                    val challengesWithProgress = allChallenges.map { challenge ->
                        if (acceptedIds.contains(challenge.id)) {
                            val challengeHabits = userHabits.filter { it.getString("sourceChallengeId") == challenge.id }
                            val allCompleted = challengeHabits.isNotEmpty() && challengeHabits.all { doc ->
                                doc.getTimestamp("lastCompletionDate")?.let { timestamp ->
                                    org.threeten.bp.Instant.ofEpochMilli(timestamp.toDate().time)
                                        .atZone(org.threeten.bp.ZoneId.systemDefault())
                                        .toLocalDate() == org.threeten.bp.LocalDate.now()
                                } ?: false
                            }
                            
                            val enrollment = enrollments.documents.firstOrNull { it.id == challenge.id }?.toObject(ChallengeEnrollment::class.java)
                            val currentDay = if (enrollment?.startDate != null) {
                                val diff = System.currentTimeMillis() - enrollment.startDate!!.time
                                (diff / (1000 * 60 * 60 * 24)).toInt() + 1
                            } else {
                                1
                            }

                            challenge.copy(
                                isCompletedToday = allCompleted,
                                currentDay = currentDay,
                                daysTotal = challenge.durationDays,
                                progressPercent = if (challenge.durationDays > 0) currentDay.toFloat() / challenge.durationDays else 0f,
                                lives = enrollment?.lives ?: 3
                            )
                        } else {
                            challenge
                        }
                    }

                    _uiState.update {
                        it.copy(
                            challenges = challengesWithProgress,
                            acceptedChallengeIds = acceptedIds, 
                            isLoading = false,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to load challenges: ${e.message}", isLoading = false) }
                }
            }
        }

        recalculateState()

        habitsListener?.remove()
        habitsListener = db.collection("users").document(userId).collection("habits")
            .addSnapshotListener { _, _ -> recalculateState() }

        enrollmentsListener?.remove()
        enrollmentsListener = db.collection("users").document(userId).collection("challengeEnrollments")
            .addSnapshotListener { _, _ -> recalculateState() }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        habitsListener?.remove()
        enrollmentsListener?.remove()
    }
}
