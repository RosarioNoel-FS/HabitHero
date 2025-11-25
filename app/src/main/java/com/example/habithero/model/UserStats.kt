package com.example.habithero.model

import com.google.firebase.firestore.PropertyName

data class UserStats(
    @get:PropertyName("totalCompleted") @set:PropertyName("totalCompleted") var totalCompleted: Int = 0,
    @get:PropertyName("habitsCreated") @set:PropertyName("habitsCreated") var habitsCreated: Int = 0,
    @get:PropertyName("activeHabits") @set:PropertyName("activeHabits") var activeHabits: Int = 0,
    @get:PropertyName("currentStreak") @set:PropertyName("currentStreak") var currentStreak: Int = 0,
    @get:PropertyName("bestStreak") @set:PropertyName("bestStreak") var bestStreak: Int = 0,
    @get:PropertyName("categoryCounts") @set:PropertyName("categoryCounts") var categoryCounts: Map<String, Int> = emptyMap()
)
