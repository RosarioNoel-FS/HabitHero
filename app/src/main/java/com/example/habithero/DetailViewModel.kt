package com.example.habithero

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.BadgeEvaluator
import com.example.habithero.data.BadgesRepository
import com.example.habithero.data.FirebaseHelper
import com.example.habithero.data.LocalBadgeCatalog
import com.example.habithero.model.Habit
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DetailUiState(
    val habit: Habit? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isDeleted: Boolean = false,
    val confettiEventId: String? = null,
    val requiresExactAlarmPermission: Boolean = false
)

class DetailViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseHelper = FirebaseHelper()
    private val badgesRepository = BadgesRepository()
    private val badgeEvaluator = BadgeEvaluator()

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

    fun onExactAlarmPermissionDialogShown() {
        _uiState.update { it.copy(requiresExactAlarmPermission = false) }
    }

    fun scheduleReminderOnResume() {
        val currentHabit = _uiState.value.habit ?: return
        if (currentHabit.reminderEnabled && notificationScheduler.canScheduleExactAlarms()) {
            viewModelScope.launch {
                try {
                    notificationScheduler.schedule(currentHabit)
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to set device alert. Please try again.") }
                }
            }
        }
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
                val habit = firebaseHelper.getHabitSuspend(userId, habitId)
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
                firebaseHelper.deleteHabitSuspend(userId, habitId)
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

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                firebaseHelper.detachHabitFromChallenge(userId, habitId)
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

        if (enabled && !notificationScheduler.canScheduleExactAlarms()) {
            _uiState.update { it.copy(requiresExactAlarmPermission = true) }
        }

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
                firebaseHelper.updateHabitReminder(userId, habitId, enabled, minutes)
                
                if (notificationScheduler.canScheduleExactAlarms()) {
                    if (optimisticallyUpdatedHabit.reminderEnabled) {
                        notificationScheduler.schedule(optimisticallyUpdatedHabit)
                    } else {
                        notificationScheduler.cancel(optimisticallyUpdatedHabit)
                    }
                }

            } catch (databaseError: Exception) {
                _uiState.update { it.copy(habit = habitBeforeUpdate, error = "Failed to save reminder.") }
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
                firebaseHelper.completeHabitAndUpdateStats(userId, habitId)
                loadHabitDetails(isCompletion = true) // Reload data and trigger confetti
                checkAndUnlockBadges(userId) // Check for new badges
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to complete habit.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private suspend fun checkAndUnlockBadges(userId: String) {
        val userStats = firebaseHelper.getUserStats(userId) ?: return
        val unlockedBadgeIds = badgesRepository.getUnlockedBadgeIds(userId)
        val allBadges = LocalBadgeCatalog.getAllBadges()

        allBadges.forEach { badge ->
            if (badge.id !in unlockedBadgeIds) { // Check only non-unlocked badges
                if (badgeEvaluator.evaluate(userStats, badge)) {
                    badgesRepository.unlockBadge(userId, badge.id)
                }
            }
        }
    }
}
