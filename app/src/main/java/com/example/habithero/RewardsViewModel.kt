package com.example.habithero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habithero.data.BadgeWithState
import com.example.habithero.data.BadgesRepository
import com.example.habithero.data.FirebaseHelper
import com.example.habithero.data.LocalBadgeCatalog
import com.example.habithero.model.UserStats
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgeSectionUiModel(
    val sectionTitle: String,
    val badges: List<BadgeWithState>
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
    private val badgesRepository = BadgesRepository()

    init {
        loadData()
    }

    private fun loadData() {
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = firebaseHelper.getUserStats(userId)
                val unlockedBadgeIds = badgesRepository.getUnlockedBadgeIds(userId)
                val allBadges = LocalBadgeCatalog.getAllBadges()

                val badgesWithState = allBadges.map {
                    BadgeWithState(
                        badge = it,
                        isUnlocked = it.id in unlockedBadgeIds
                    )
                }

                val badgeSections = badgesWithState
                    .groupBy { it.badge.categoryKey }
                    .map { (category, badges) ->
                        BadgeSectionUiModel(category, badges.sortedBy { it.badge.sortOrder })
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
