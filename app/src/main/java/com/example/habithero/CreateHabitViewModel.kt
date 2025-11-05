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

data class CreateHabitUiState(
    val habitName: String = "",
    val habitEmoji: String = "",
    val selectedCategory: String = "",
    val iconUrl: String = "", // Added to hold the category icon URL
    val categories: Map<String, String> = emptyMap(), // Store name-to-URL map
    val selectedHour: Int = 21,
    val selectedMinute: Int = 0,
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
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val categoriesResult = firebaseHelper.fetchCategoriesSuspend()
                val categoryMap = categoriesResult.mapValues { it.value.iconUrl }
                _uiState.update { it.copy(categories = categoryMap) }

                if (_uiState.value.isEditMode && habitId != null && userId != null) {
                    originalHabit = firebaseHelper.getHabitSuspend(userId, habitId)
                    if (originalHabit != null) {
                        _uiState.update {
                            it.copy(
                                habitName = originalHabit!!.name,
                                habitEmoji = originalHabit!!.emoji,
                                selectedCategory = originalHabit!!.category,
                                iconUrl = originalHabit!!.iconUrl, // Load the icon URL
                                selectedHour = originalHabit!!.completionHour,
                                selectedMinute = originalHabit!!.completionMinute,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(error = "Habit not found", isLoading = false) }
                    }
                } else {
                     _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load data", isLoading = false) }
            }
        }
    }

    fun onHabitNameChanged(newName: String) {
        _uiState.update { it.copy(habitName = newName) }
    }

    fun onHabitEmojiChanged(newEmoji: String) {
        _uiState.update { it.copy(habitEmoji = newEmoji) }
    }

    // Updated to handle both name and URL
    fun onCategorySelected(name: String, url: String) {
        _uiState.update { it.copy(selectedCategory = name, iconUrl = url) }
    }

    fun onTimeChanged(hour: Int, minute: Int) {
        _uiState.update { it.copy(selectedHour = hour, selectedMinute = minute) }
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
                if (currentState.isEditMode) {
                    val updatedHabit = (originalHabit ?: Habit()).copy(
                        name = currentState.habitName,
                        emoji = currentState.habitEmoji,
                        category = currentState.selectedCategory,
                        iconUrl = currentState.iconUrl, // Save the icon URL
                        completionHour = currentState.selectedHour,
                        completionMinute = currentState.selectedMinute
                    )
                    updatedHabit.id = habitId!!
                    firebaseHelper.updateHabitSuspend(userId, updatedHabit)
                } else {
                     val newHabit = Habit(
                        name = currentState.habitName,
                        emoji = currentState.habitEmoji,
                        category = currentState.selectedCategory,
                        iconUrl = currentState.iconUrl, // Save the icon URL
                        completionHour = currentState.selectedHour,
                        completionMinute = currentState.selectedMinute
                    )
                    firebaseHelper.addHabitSuspend(userId, newHabit)
                }
                _uiState.update { it.copy(isHabitCreatedOrUpdated = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save habit") }
            }
        }
    }
}
