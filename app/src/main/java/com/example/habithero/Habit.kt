package com.example.habithero

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

    /**
     * Calculates the current streak by iterating backwards from today.
     * This is a computed property, ensuring the streak is always up-to-date.
     */
    @get:Exclude
    val streakCount: Int
        get() {
            if (completionDates.isEmpty()) return 0

            // Use a mutable, sorted list of unique LocalDates for easier processing
            val uniqueDates = completionDates
                .map { Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate() }
                .toSet()
                .sortedDescending()

            var streak = 0
            var currentDate = LocalDate.now()
            val now = LocalDateTime.now()

            // Define today's deadline
            val todayDeadline = currentDate.atTime(completionHour, completionMinute)

            var expectedDate = currentDate

            // If the time now is BEFORE today's deadline, the current period is for YESTERDAY's date.
            if (now < todayDeadline) {
                expectedDate = currentDate.minusDays(1)
            }

            // Now, iterate through the completion dates and see how long the chain is.
            for (date in uniqueDates) {
                if (date == expectedDate) {
                    streak++
                    expectedDate = expectedDate.minusDays(1) // Expect the day before for the next loop
                } else if (date < expectedDate) {
                    // A gap was found, the streak is broken.
                    break
                }
            }

            return streak
        }
}
