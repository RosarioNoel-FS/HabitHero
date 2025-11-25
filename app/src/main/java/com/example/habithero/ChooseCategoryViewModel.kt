package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.FirebaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChooseCategoryUiState(
    val categoryInfos: List<CategoryInfo> = emptyList(), // Renamed from 'categories'
    val isLoading: Boolean = true
)

class ChooseCategoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChooseCategoryUiState())
    val uiState: StateFlow<ChooseCategoryUiState> = _uiState.asStateFlow()

    // Expose a map of name to icon URL for other parts of the app to use
    private val _categories = MutableStateFlow<Map<String, String>>(emptyMap())
    val categories: StateFlow<Map<String, String>> = _categories.asStateFlow()

    private val firebaseHelper = FirebaseHelper()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val categoriesResult = firebaseHelper.fetchCategoriesSuspend()

                // Populate the public map
                _categories.value = categoriesResult.mapValues { it.value.iconUrl }

                // Populate the UI state's list for the ChooseCategoryScreen itself
                val categoryList = categoriesResult.map { (_, cat) ->
                    val description = when(cat.name) {
                        "Health & Fitness" -> "Physical wellness and exercise habits"
                        "Mindfulness & Wellbeing" -> "Mental health and meditation practices"
                        "Learning & Growth" -> "Knowledge and skill development"
                        "Creativity & Expression" -> "Artistic and creative pursuits"
                        "Adventure & Exploration" -> "New experiences and adventures"
                        else -> "Design a custom habit that fits your unique goals"
                    }
                    CategoryInfo(cat.name, description, cat.iconUrl)
                }.filter { it.name != "Create Your Own" }

                val finalCategories = categoryList.toMutableList().apply {
                    add(CategoryInfo("Create Your Own", "Design a custom habit that fits your unique goals", "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fcustom.png?alt=media&token=9497217b-2b82-4f49-9291-32c736f9df55"))
                }

                _uiState.update { it.copy(categoryInfos = finalCategories, isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
