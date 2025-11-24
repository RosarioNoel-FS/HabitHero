package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.model.Challenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// The UI State now holds the list of all challenges, fetched from Firestore.
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

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, load all challenge data from Firestore.
                loadAllChallengesFromFirestore(user.uid)
            } else {
                // User is signed out, clear the UI.
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

    private fun loadAllChallengesFromFirestore(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Step 1: Fetch all challenges from the top-level /challenges collection.
                // This will now succeed because of the new security rule.
                val challengesSnapshot = db.collection("challenges").get().await()
                val allChallenges = challengesSnapshot.documents.mapNotNull { document ->
                    document.toObject(Challenge::class.java)?.copy(id = document.id)
                }

                // Step 2: Fetch the user's accepted challenges to mark them in the UI.
                val enrollmentsSnapshot = db.collection("users").document(userId)
                    .collection("challengeEnrollments").get().await()
                val acceptedIds = enrollmentsSnapshot.documents.map { it.id }.toSet()

                _uiState.update {
                    it.copy(
                        challenges = allChallenges,
                        acceptedChallengeIds = acceptedIds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // If this fails now, it's likely because the security rule was not deployed.
                _uiState.update { it.copy(error = "Failed to load challenges. Did you update your Firestore security rules? Error: ${e.message}", isLoading = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}
