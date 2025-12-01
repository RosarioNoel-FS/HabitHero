package com.example.habithero.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserBadge(
    val badgeId: String = "",
    @ServerTimestamp val unlockedAt: Date? = null
)
