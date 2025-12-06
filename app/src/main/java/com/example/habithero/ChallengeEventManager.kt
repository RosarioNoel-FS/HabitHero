package com.example.habithero

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persisting and retrieving one-time challenge-related events,
 * like a life being lost, so the app can show a dialog after the fact.
 */
class ChallengeEventManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ChallengeEvents", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LIVES_LOST = "lives_lost_challenge_ids"
    }

    /**
     * Records that a life was lost for a given challenge ID.
     * We store a set to prevent duplicate events if the worker runs multiple times.
     */
    fun recordLifeLost(challengeId: String) {
        val currentSet = prefs.getStringSet(KEY_LIVES_LOST, emptySet()) ?: emptySet()
        val newSet = currentSet.toMutableSet().apply { add(challengeId) }
        prefs.edit().putStringSet(KEY_LIVES_LOST, newSet).apply()
    }

    /**
     * Retrieves the list of challenge IDs for which a life was lost and
     * immediately clears the events to prevent them from being shown again.
     */
    fun getAndClearLifeLostEvents(): Set<String> {
        val events = prefs.getStringSet(KEY_LIVES_LOST, emptySet()) ?: emptySet()
        if (events.isNotEmpty()) {
            prefs.edit().remove(KEY_LIVES_LOST).apply()
        }
        return events
    }
}
