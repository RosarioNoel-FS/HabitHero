package com.example.habithero

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class DetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class DetailViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val habitId: String = checkNotNull(savedStateHandle["habitId"])
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val notificationScheduler = NotificationScheduler(getApplication())

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
                val habitDocument = db.collection("users").document(userId)
                    .collection("habits").document(habitId).get().await()
                
                val habit = habitDocument.toObject(Habit::class.java)?.apply {
                    id = habitDocument.id
                }

                if (habit != null) {
                     _uiState.update { it.copy(habit = habit, isLoading = false) }
                } else {
                     if (_uiState.value.isDeleted) return@launch
                     _uiState.update { it.copy(error = "Habit not found.", isLoading = false) }
                }
               
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load habit details: ${e.message}", isLoading = false) }
            }
        }
    }

    fun deleteHabit() {
        val habitForNotification = _uiState.value.habit
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        viewModelScope.launch {
            try {
                // Cancel notification if we have a habit object
                if (habitForNotification != null) {
                    notificationScheduler.cancel(habitForNotification)
                }

                // **THE FIX**: Always use the immutable `habitId` from the SavedStateHandle.
                // This ensures we always delete the correct document, regardless of UI state changes.
                db.collection("users").document(userId).collection("habits").document(habitId).delete().await()

                // Set isDeleted to true to trigger navigation from the UI
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete habit.") }
            }
        }
    }
    
    fun detachHabitFromChallenge() {
        if (userId == null) return
        val currentHabit = _uiState.value.habit ?: return

        val updatedHabit = currentHabit.copy(
            sourceChallengeId = null,
            sourceTemplateId = null
        )

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                _uiState.update { it.copy(habit = updatedHabit) } 
            } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to detach habit.") }
            }
        }
    }

    fun updateReminder(isEnabled: Boolean) {
        if (userId == null) return
        val currentHabit = _uiState.value.habit ?: return

        val updatedHabit = currentHabit.copy(reminderEnabled = isEnabled)

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                _uiState.update { it.copy(habit = updatedHabit) }

                if (updatedHabit.reminderEnabled) {
                    notificationScheduler.schedule(updatedHabit)
                } else {
                    notificationScheduler.cancel(updatedHabit)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update reminder.") }
            }
        }
    }
    
    fun updateReminderTime(minutes: Int) {
        if (userId == null) return
        val currentHabit = _uiState.value.habit ?: return

        val updatedHabit = currentHabit.copy(reminderTimeMinutes = minutes)

        viewModelScope.launch {
            try {
                 db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                _uiState.update { it.copy(habit = updatedHabit) }

                if (updatedHabit.reminderEnabled) {
                    notificationScheduler.schedule(updatedHabit)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update reminder time.") }
            }
        }
    }

    fun completeHabit() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }
        val currentHabit = _uiState.value.habit ?: return

        val newCompletionDates = currentHabit.completionDates + Date()

        val updatedHabit = currentHabit.copy(
            completionDates = newCompletionDates,
            completionCount = currentHabit.completionCount + 1
        )

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                _uiState.update { it.copy(habit = updatedHabit) }
            } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to complete habit.") }
            }
        }
    }
}
