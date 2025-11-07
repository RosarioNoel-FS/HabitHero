package com.example.habithero

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val durationDays: Int,
    val icon: String, // Use a simple name to map to a drawable
    val habits: List<HabitTemplate>,
    val about: String,
    val whyItMatters: String,
    val positiveEffects: List<String>
)

data class HabitTemplate(
    val name: String,
    val category: String,
    val emoji: String,
    val completionHour: Int,
    val completionMinute: Int,
    val iconUrl: String
)
