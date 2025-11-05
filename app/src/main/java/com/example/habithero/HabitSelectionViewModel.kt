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

data class HabitSelectionUiState(
    val habitList: List<Habit> = emptyList(),
    val category: Category? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val habitAdded: Boolean = false
)

class HabitSelectionViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val categoryName: String = checkNotNull(savedStateHandle["categoryName"])
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(HabitSelectionUiState())
    val uiState: StateFlow<HabitSelectionUiState> = _uiState.asStateFlow()

    private val firebaseHelper = FirebaseHelper()

    init {
        loadHabitSelectionData()
    }

    private fun loadHabitSelectionData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val categories = firebaseHelper.fetchCategoriesSuspend()
                val currentCategory = categories[categoryName]

                if (currentCategory != null) {
                    val habits = currentCategory.habitList.map {
                        Habit(name = it, category = categoryName, iconUrl = currentCategory.iconUrl)
                    }
                    _uiState.update { it.copy(category = currentCategory, habitList = habits, isLoading = false) }
                } else {
                    _uiState.update { it.copy(error = "Category not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load data", isLoading = false) }
            }
        }
    }

    fun addHabit(habit: Habit, hour: Int, minute: Int) {
        if (userId == null) {
            _uiState.update { it.copy(error = "User not logged in") }
            return
        }

        val newHabit = habit.copy(
            completionHour = hour,
            completionMinute = minute
        )

        viewModelScope.launch {
            try {
                firebaseHelper.addHabitSuspend(userId, newHabit)
                _uiState.update { it.copy(habitAdded = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add habit") }
            }
        }
    }
}
