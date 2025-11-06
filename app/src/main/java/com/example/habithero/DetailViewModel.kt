package com.example.habithero

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class DetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val firebaseHelper = FirebaseHelper()

    init {
        loadHabitDetails()
    }

    fun refresh() {
        loadHabitDetails()
    }

    private fun loadHabitDetails() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in", isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val habit = firebaseHelper.getHabitSuspend(userId, habitId)
                _uiState.update { it.copy(habit = habit, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load habit details.", isLoading = false) }
            }
        }
    }

    fun deleteHabit() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            try {
                firebaseHelper.deleteHabitSuspend(userId, habitId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete habit.") }
            }
        }
    }

    fun completeHabit() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }
        val currentHabit = _uiState.value.habit ?: return

        // Create a new list of completion dates including the new one.
        val newCompletionDates = currentHabit.completionDates + Date()

        // Create the updated habit object.
        // The streak is now calculated dynamically in the Habit data class.
        val updatedHabit = currentHabit.copy(
            completionDates = newCompletionDates,
            completionCount = currentHabit.completionCount + 1
        )
        updatedHabit.id = currentHabit.id // Preserve the habit ID

        viewModelScope.launch {
            try {
                // Use the general update method to save the entire updated habit
                firebaseHelper.updateHabitSuspend(userId, updatedHabit)
                _uiState.update { it.copy(habit = updatedHabit) }
            } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to complete habit.") }
            }
        }
    }
}
