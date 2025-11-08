package com.example.habithero

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
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

class DetailViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val firebaseHelper = FirebaseHelper()
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
                _uiState.value.habit?.let { notificationScheduler.cancel(it) }
                firebaseHelper.deleteHabitSuspend(userId, habitId)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete habit.") }
            }
        }
    }

    fun updateReminder(isEnabled: Boolean) {
        if (userId == null) return
        val currentHabit = _uiState.value.habit ?: return

        val updatedHabit = currentHabit.copy(reminderEnabled = isEnabled)
        updatedHabit.id = currentHabit.id

        viewModelScope.launch {
            try {
                firebaseHelper.updateHabitSuspend(userId, updatedHabit)
                _uiState.update { it.copy(habit = updatedHabit) }
                // Schedule or cancel notification
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
        updatedHabit.id = currentHabit.id

        viewModelScope.launch {
            try {
                firebaseHelper.updateHabitSuspend(userId, updatedHabit)
                _uiState.update { it.copy(habit = updatedHabit) }
                // Reschedule notification with the new time
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
        updatedHabit.id = currentHabit.id

        viewModelScope.launch {
            try {
                firebaseHelper.updateHabitSuspend(userId, updatedHabit)
                _uiState.update { it.copy(habit = updatedHabit) }
            } catch (e: Exception) {
                 _uiState.update { it.copy(error = "Failed to complete habit.") }
            }
        }
    }
}
