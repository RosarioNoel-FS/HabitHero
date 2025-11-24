package com.example.habithero

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.FirebaseHelper
import com.example.habithero.model.Habit
import com.example.habithero.model.UserStats
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateHabitUiState(
    val habitName: String = "",
    val habitEmoji: String = "",
    val selectedCategory: String = "",
    val iconUrl: String = "",
    val categories: Map<String, String> = emptyMap(),
    val selectedHour: Int = 21,
    val selectedMinute: Int = 0,
    // Reminder Fields
    val reminderEnabled: Boolean = false,
    val reminderTimeMinutes: Int = 15, // Default offset

    val isLoading: Boolean = true,
    val isHabitCreatedOrUpdated: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

class CreateHabitViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val habitId: String? = savedStateHandle["habitId"]
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(CreateHabitUiState())
    val uiState: StateFlow<CreateHabitUiState> = _uiState.asStateFlow()

    private val firebaseHelper = FirebaseHelper()
    private var originalHabit: Habit? = null

    init {
        val isEditMode = habitId != null
        _uiState.update { it.copy(isEditMode = isEditMode) }
        // The initial load is now correctly handled by the view's lifecycle.
    }

    fun loadHabitData() {
        viewModelScope.launch {
            // 1. Set loading state to true at the beginning of the process.
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Fetch categories first, as they are needed in both modes.
                val categoriesResult = firebaseHelper.fetchCategoriesSuspend()
                val categoryMap = categoriesResult.mapValues { it.value.iconUrl }

                if (_uiState.value.isEditMode && habitId != null && userId != null) {
                    // If in edit mode, fetch the habit
                    originalHabit = firebaseHelper.getHabitSuspend(userId, habitId)
                    if (originalHabit != null) {
                        // 2. CRITICAL FIX: Update all fields from the loaded habit in a single update.
                        _uiState.update {
                            it.copy(
                                habitName = originalHabit!!.name,
                                habitEmoji = originalHabit!!.emoji,
                                selectedCategory = originalHabit!!.category,
                                iconUrl = originalHabit!!.iconUrl,
                                selectedHour = originalHabit!!.completionHour,
                                selectedMinute = originalHabit!!.completionMinute,
                                reminderEnabled = originalHabit!!.reminderEnabled,      // This was the missing piece
                                reminderTimeMinutes = originalHabit!!.reminderTimeMinutes, // This was the missing piece
                                categories = categoryMap,
                                isLoading = false // Set loading to false only after all data is loaded
                            )
                        }
                    } else {
                        // Habit not found, stop loading and show an error
                        _uiState.update { it.copy(error = "Habit not found", isLoading = false, categories = categoryMap) }
                    }
                } else {
                    // Not in edit mode, just load categories and stop loading
                     _uiState.update { it.copy(categories = categoryMap, isLoading = false) }
                }
            } catch (e: Exception) {
                // Handle any exceptions during the process
                _uiState.update { it.copy(error = "Failed to load data: ${e.message}", isLoading = false) }
            }
        }
    }

    fun onHabitNameChanged(newName: String) {
        _uiState.update { it.copy(habitName = newName) }
    }

    fun onHabitEmojiChanged(newEmoji: String) {
        _uiState.update { it.copy(habitEmoji = newEmoji) }
    }

    fun onCategorySelected(name: String, url: String) {
        _uiState.update { it.copy(selectedCategory = name, iconUrl = url) }
    }

    fun onTimeChanged(hour: Int, minute: Int) {
        _uiState.update { it.copy(selectedHour = hour, selectedMinute = minute) }
    }

    fun onReminderChanged(enabled: Boolean, minutes: Int) {
        _uiState.update { it.copy(reminderEnabled = enabled, reminderTimeMinutes = minutes) }
    }

    fun saveHabit() {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        val currentState = _uiState.value
        if (currentState.habitName.isBlank() || currentState.selectedCategory.isBlank()) {
            _uiState.update { it.copy(error = "Habit name and category cannot be empty") }
            return
        }

        viewModelScope.launch {
            try {
                val habitToSave = if (currentState.isEditMode) {
                    (originalHabit ?: Habit()).copy(
                        name = currentState.habitName,
                        emoji = currentState.habitEmoji,
                        category = currentState.selectedCategory,
                        iconUrl = currentState.iconUrl,
                        completionHour = currentState.selectedHour,
                        completionMinute = currentState.selectedMinute,
                        reminderEnabled = currentState.reminderEnabled,
                        reminderTimeMinutes = currentState.reminderTimeMinutes
                    ).apply {
                        id = habitId!!
                    }
                } else {
                    Habit(
                        name = currentState.habitName,
                        emoji = currentState.habitEmoji,
                        category = currentState.selectedCategory,
                        iconUrl = currentState.iconUrl,
                        completionHour = currentState.selectedHour,
                        completionMinute = currentState.selectedMinute,
                        reminderEnabled = currentState.reminderEnabled,
                        reminderTimeMinutes = currentState.reminderTimeMinutes
                    )
                }

                if (currentState.isEditMode) {
                    firebaseHelper.updateHabitSuspend(userId, habitToSave)
                } else {
                    firebaseHelper.addHabitAndUpdateStats(userId, habitToSave)
                }

                _uiState.update { it.copy(isHabitCreatedOrUpdated = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save habit: ${e.message}") }
            }
        }
    }
}
