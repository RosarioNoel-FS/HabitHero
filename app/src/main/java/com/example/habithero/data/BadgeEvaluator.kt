package com.example.habithero.data

import com.example.habithero.model.Badge
import com.example.habithero.model.UserStats

class BadgeEvaluator {

    fun evaluate(userStats: UserStats, badge: Badge): Boolean {
        return badge.criteria.all { (statKey, requiredValue) ->
            val userValue = when (statKey) {
                "totalCompleted" -> userStats.totalCompleted
                "currentStreak" -> userStats.currentStreak
                "bestStreak" -> userStats.bestStreak
                "activeHabits" -> userStats.activeHabits
                "habitsCreated" -> userStats.habitsCreated
                else -> 0
            }
            userValue >= requiredValue
        }
    }
}
