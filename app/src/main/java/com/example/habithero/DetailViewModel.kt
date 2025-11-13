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
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.Date
import java.util.UUID

data class DetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val confettiEventId: String? = null
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

    fun onConfettiShown() {
        _uiState.update { it.copy(confettiEventId = null) }
    }

    private fun loadHabitDetails(isCompletion: Boolean = false) {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in", isLoading = false) }
            return
        }

        viewModelScope.launch {
            if (_uiState.value.habit == null) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val habitDocument = db.collection("users").document(userId)
                    .collection("habits").document(habitId).get().await()

                val habit = habitDocument.toObject(Habit::class.java)?.apply {
                    id = habitDocument.id
                }

                if (habit != null) {
                    _uiState.update {
                        it.copy(
                            habit = habit,
                            isLoading = false,
                            confettiEventId = if (isCompletion) UUID.randomUUID().toString() else it.confettiEventId
                        )
                    }
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
                if (habitForNotification != null) {
                    notificationScheduler.cancel(habitForNotification)
                }
                db.collection("users").document(userId).collection("habits").document(habitId).delete().await()
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
                loadHabitDetails()
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

                if (updatedHabit.reminderEnabled) {
                    notificationScheduler.schedule(updatedHabit)
                } else {
                    notificationScheduler.cancel(updatedHabit)
                }
                loadHabitDetails()
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
                loadHabitDetails()

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
        if (currentHabit.isCompletedToday) return // Already completed

        val newCompletionDates = currentHabit.completionDates + Date()

        // The `streakCount` is a computed property on the Habit model, so we don't need to calculate it here.
        // We just need to update the completion dates.
        val updatedHabit = currentHabit.copy(
            completionDates = newCompletionDates,
            completionCount = currentHabit.completionCount + 1
        )

        viewModelScope.launch {
            try {
                db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                // Reload from server to ensure consistency and trigger confetti event
                loadHabitDetails(isCompletion = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to complete habit.") }
            }
        }
    }
}
