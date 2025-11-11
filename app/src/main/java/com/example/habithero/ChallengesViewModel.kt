package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// The ViewModel's state only needs to care about which challenges are accepted.
// The list of all possible challenges comes from the local ChallengeData object.
data class ChallengesUiState(
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
                // User is signed in, so we can safely fetch their private data.
                fetchAcceptedChallenges(user.uid)
            } else {
                // User is signed out, clear the state and show a message.
                _uiState.update {
                    it.copy(
                        acceptedChallengeIds = emptySet(),
                        isLoading = false,
                        error = "You must be logged in to view challenges."
                    )
                }
            }
        }
        auth.addAuthStateListener(authStateListener)
    }

    private fun fetchAcceptedChallenges(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // This query is allowed by security rules because it only accesses the logged-in user's data.
                val enrollmentsSnapshot = db.collection("users").document(userId)
                    .collection("challengeEnrollments").get().await()
                val acceptedIds = enrollmentsSnapshot.documents.map { it.id }.toSet()

                _uiState.update {
                    it.copy(
                        acceptedChallengeIds = acceptedIds,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // This will now only happen if fetching the user's private data fails.
                _uiState.update { it.copy(error = "Failed to load your challenge status: ${e.message}", isLoading = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Important: Remove the listener to prevent memory leaks.
        auth.removeAuthStateListener(authStateListener)
    }
}
