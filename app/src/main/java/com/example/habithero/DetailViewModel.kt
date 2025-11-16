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
import java.util.UUID

data class DetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false, // The new flag to prevent race conditions
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

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
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
             _uiState.update { it.copy(isSaving = true) }
            try {
                if (habitForNotification != null) {
                    notificationScheduler.cancel(habitForNotification)
                }
                db.collection("users").document(userId).collection("habits").document(habitId).delete().await()
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete habit.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun detachHabitFromChallenge() {
        if (userId == null) return
        val currentHabit = _uiState.value.habit ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                 val updatedHabit = currentHabit.copy(
                    sourceChallengeId = null,
                    sourceTemplateId = null
                ).also { it.id = currentHabit.id }

                db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                loadHabitDetails()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to detach habit.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun updateReminder(enabled: Boolean, minutes: Int) {
        if (userId == null) {
            _uiState.update { it.copy(error = "You're not logged in.") }
            return
        }
        val habitBeforeUpdate = _uiState.value.habit ?: return

        val optimisticallyUpdatedHabit = habitBeforeUpdate.copy(
            reminderEnabled = enabled, 
            reminderTimeMinutes = minutes
        ).also { 
            it.id = habitBeforeUpdate.id
        }
        
        _uiState.update { it.copy(habit = optimisticallyUpdatedHabit, error = null) }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val updates = if (enabled) {
                    mapOf("reminderEnabled" to true, "reminderTimeMinutes" to minutes)
                } else {
                    mapOf("reminderEnabled" to false)
                }
                db.collection("users").document(userId).collection("habits").document(habitId).update(updates).await()
                
                try {
                    if (optimisticallyUpdatedHabit.reminderEnabled) {
                        notificationScheduler.schedule(optimisticallyUpdatedHabit)
                    } else {
                        notificationScheduler.cancel(optimisticallyUpdatedHabit)
                    }
                } catch (schedulingError: Exception) {
                    _uiState.update { it.copy(error = "Reminder saved, but failed to set device alert.") }
                }

            } catch (databaseError: Exception) {
                _uiState.update { it.copy(habit = habitBeforeUpdate, error = "Failed to save reminder to cloud.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun completeHabit() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }
        val currentHabit = _uiState.value.habit ?: return
        if (currentHabit.isCompletedToday) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val newCompletionDates = currentHabit.completionDates + Date()
                val updatedHabit = currentHabit.copy(
                    completionDates = newCompletionDates,
                    completionCount = currentHabit.completionCount + 1
                ).also {
                    it.id = currentHabit.id
                }
                db.collection("users").document(userId).collection("habits").document(habitId)
                    .set(updatedHabit).await()
                loadHabitDetails(isCompletion = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to complete habit.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
