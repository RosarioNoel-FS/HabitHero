package com.example.habithero.data

import com.example.habithero.model.Habit

data class HabitWithCompletion(
    val habit: Habit,
    val isCompletedToday: Boolean
)
