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

            // If it's already past today's deadline and the habit hasn't been completed today, the streak is 0.
            // But we must first check if the most recent completion was for today's period.
            val mostRecentCompletion = if (uniqueDates.isNotEmpty()) uniqueDates.first() else null

            // Determine the start date for the streak check.
            // If the latest completion is today, we start checking from today.
            // If the latest completion was yesterday, we also start from today to check continuity.
            // If it was before yesterday, the streak is broken unless we are checking the day right after that completion.

            var expectedDate = currentDate

            // If the time now is BEFORE today's deadline, the current period is for YESTERDAY's date.
            // So, we should check if yesterday was completed.
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
                // If date > expectedDate, it means multiple completions on the same day, which is fine.
            }

            return streak
        }
}
