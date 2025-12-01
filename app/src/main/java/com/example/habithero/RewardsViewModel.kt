package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.FirebaseHelper
import com.example.habithero.data.LocalBadgeCatalog
import com.example.habithero.model.Badge
import com.example.habithero.model.UserStats
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgeSectionUiModel(
    val sectionTitle: String,
    val badges: List<Badge>
)

data class RewardsUiState(
    val userStats: UserStats? = null,
    val badgeSections: List<BadgeSectionUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class RewardsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseHelper = FirebaseHelper()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = if (userId != null) firebaseHelper.getUserStats(userId) else null
                val allBadges = LocalBadgeCatalog.getAllBadges()

                val badgeSections = allBadges
                    .groupBy { it.categoryKey }
                    .map { (category, badges) ->
                        BadgeSectionUiModel(category, badges.sortedBy { it.sortOrder })
                    }
                    .sortedBy { it.sectionTitle } // Sort sections alphabetically

                _uiState.update {
                    it.copy(
                        userStats = stats,
                        badgeSections = badgeSections,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load data: ${e.message}", isLoading = false) }
            }
        }
    }
}
