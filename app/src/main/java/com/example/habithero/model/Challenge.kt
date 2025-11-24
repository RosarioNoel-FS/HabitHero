package com.example.habithero.model

import com.google.firebase.firestore.PropertyName

/**
 * Represents a challenge in the app. The default values are required for Firestore
 * to be able to deserialize the data into this object. They create a no-argument
 * constructor that Firestore can use.
 */
data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val durationDays: Int = 0,
    val icon: String = "",
    @get:PropertyName("iconUrl") @set:PropertyName("iconUrl") var iconUrl: String = "",
    val habits: List<HabitTemplate> = emptyList(),
    val about: String = "",
    val whyItMatters: String = "",
    val positiveEffects: List<String> = emptyList()
)

/**
 * Represents a single habit that is part of a challenge. The default values are
 * required for Firestore deserialization.
 */
data class HabitTemplate(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val emoji: String = "",
    val completionHour: Int = 0,
    val completionMinute: Int = 0,
    @get:PropertyName("iconUrl") @set:PropertyName("iconUrl") var iconUrl: String = 