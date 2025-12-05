package com.example.habithero.model

import com.example.habithero.data.HabitWithCompletion

data class ChallengeProgress(
    val challengeName: String,
    val habits: List<HabitWithCompletion>,
    val newlyCompletedHabitId: String
)
