package com.example.habithero.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.io.Serializable
import java.util.Date

data class Habit(
    var name: String = "",
    var category: String = "",
    var emoji: String = "",
    var completionHour: Int = 21, // Default to 9 PM
    var completionMinute: Int = 0,
    var iconUrl: String = "",
    var completionCount: Int = 0,
    var completionDates: List<Date> = emptyList(),
    // New fields for reminders
    var reminderEnabled: Boolean = false, // Default to off
    var reminderTimeMinutes: Int = 15, // Default to 15 minutes before deadline
    // New fields for deterministic habit creation from challenges
    var sourceChallengeId: String? = null,
    var sourceTemplateId: String? = null,
    // Deprecated fields, kept for Firestore compatibility but not used in new logic
    var completed: Boolean = false,
    var timestamp: Timestamp? = null
) : Serializable {

    @get:Exclude
    @set:Exclude
    var id: String = ""

    @get:Exclude
    val lastCompletionDate: Date?
        get() = completionDates.maxOrNull()

    /**
     * Determines if the habit has been completed within the current 24-hour period
     * defined by the habit's deadline.
     */
    @get:Exclude
    val isCompletedToday: Boolean
        get() {
            val lastCompletion = lastCompletionDate ?: return false

            val now = LocalDateTime.now()
            val today = LocalDate.now()

            // Determine the deadline for the current 24-hour cycle.
            val deadlineForToday = today.atTime(completionHour, completionMinute)

            val (periodStart, periodEnd) = if (now < deadlineForToday) {
                // We are in the period that started yesterday.
                val deadlineForYesterday = deadlineForToday.minusDays(1)
                deadlineForYesterday to deadlineForToday
            } else {
                // We are in the period that started today.
                val deadlineForTomorrow = deadlineForToday.plusDays(1)
                deadlineForToday to deadlineForTomorrow
            }

            val lastCompletionDateTime = Instant.ofEpochMilli(lastCompletion.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            return lastCompletionDateTime >= periodStart && lastCompletionDateTime < periodEnd
        }

    @get:Exclude
    val streakCount: Int
        get() = calculateStreak(completionDates)

    fun calculateStreak(dates: List<Date>): Int {
        if (dates.isEmpty()) return 0

        val effectiveDates = dates.map { date ->
            val completionDateTime = Instant.ofEpochMilli(date.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val deadlineForCompletionDay = completionDateTime.toLocalDate().atTime(completionHour, completionMinute)

            if (completionDateTime < deadlineForCompletionDay) {
                completionDateTime.toLocalDate().minusDays(1)
            } else {
                completionDateTime.toLocalDate()
            }
        }

        val uniqueEffectiveDates = effectiveDates.toSet().sortedDescending()

        var streak = 0
        val now = LocalDateTime.now()
        val today = LocalDate.now()

        var expectedDate = if (now < today.atTime(completionHour, completionMinute)) {
            today.minusDays(1)
        } else {
            today
        }

        for (date in uniqueEffectiveDates) {
            if (date == expectedDate) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else if (date < expectedDate) {
                break
            }
        }

        return streak
    }
}
