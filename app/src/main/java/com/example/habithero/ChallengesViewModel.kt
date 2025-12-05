package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.model.Challenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

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

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                subscribeToHabitUpdates(user.uid)
            } else {
                habitsListener?.remove()
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

    private fun subscribeToHabitUpdates(userId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        habitsListener?.remove()
        habitsListener = db.collection("users").document(userId).collection("habits")
            .addSnapshotListener { habitsSnapshot, error ->
                if (error != null || habitsSnapshot == null) {
                    _uiState.update { it.copy(error = "Failed to load habits.", isLoading = false) }
                    return@addSnapshotListener
                }

                viewModelScope.launch {
                    try {
                        val allChallenges = db.collection("challenges").get().await()
                            .documents.mapNotNull { doc -> doc.toObject(Challenge::class.java)?.copy(id = doc.id) }

                        val enrollments = db.collection("users").document(userId)
                            .collection("challengeEnrollments").get().await()
                        val acceptedIds = enrollments.documents.map { it.id }.toSet()

                        val userHabits = habitsSnapshot.documents

                        val challengesWithProgress = allChallenges.map { challenge ->
                            if (acceptedIds.contains(challenge.id)) {
                                val challengeHabits = userHabits.filter { it.getString("sourceChallengeId") == challenge.id }
                                val allCompleted = challengeHabits.isNotEmpty() && challengeHabits.all { doc ->
                                    doc.getTimestamp("lastCompletionDate")?.let { timestamp ->
                                        Instant.ofEpochMilli(timestamp.toDate().time)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate() == LocalDate.now()
                                    } ?: false
                                }
                                challenge.copy(isCompletedToday = allCompleted)
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
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        habitsListener?.remove()
    }
}
