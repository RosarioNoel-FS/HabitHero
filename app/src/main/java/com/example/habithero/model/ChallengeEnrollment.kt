package com.example.habithero.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChallengeEnrollment(
    val challengeId: String = "",
    val status: String = "ACTIVE", // e.g., ACTIVE, COMPLETED, DROPPED
    @ServerTimestamp
    val startDate: Date? = null,
    val successDays: Int = 0,
    val missedDays: Int = 0,
    val lastEvaluatedDate: Date? = null,
    val lives: Int = 3 // Add the lives property with a default value
) {
    // No-argument constructor for Firestore
    constructor() : this("", "ACTIVE", null, 0, 0, null, 3)
}
