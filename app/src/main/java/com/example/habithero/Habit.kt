package com.example.habithero

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.Calendar
import java.util.Date

// Corrected data class with @JvmOverloads to ensure Java compatibility
data class Habit @JvmOverloads constructor(
    var name: String = "",
    var category: String = "",
    var emoji: String = "",
    var completionHour: Int = 0,
    var completionMinute: Int = 0,
    var iconUrl: String = "",
    var completed: Boolean = false,
    var timestamp: Timestamp? = Timestamp.now(),
    var streakCount: Int = 0,
    var completionCount: Int = 0,
    var completionDates: List<Date> = emptyList()
) : Serializable {

    @get:Exclude
    @set:Exclude
    var id: String = ""

    @get:Exclude
    val lastCompletionDate: Date?
        get() = completionDates.lastOrNull()

    /**
     * Returns a new Habit object with updated completion stats.
     * This is a pure function and does not modify the original habit.
     */
    fun habitCompleted(): Habit {
        val now = Date()
        val lastCompletion = lastCompletionDate

        val deadlineCal = Calendar.getInstance()
        deadlineCal.time = now // Start with today's date for calculation
        deadlineCal.set(Calendar.HOUR_OF_DAY, this.completionHour)
        deadlineCal.set(Calendar.MINUTE, this.completionMinute)
        deadlineCal.set(Calendar.SECOND, 0)
        deadlineCal.set(Calendar.MILLISECOND, 0)

        // The current completion period started 24 hours before the next deadline.
        // If the deadline for today is 9 PM, the period started at 9 PM yesterday.
        val periodEnd = deadlineCal.time
        deadlineCal.add(Calendar.DAY_OF_YEAR, -1)
        val periodStart = deadlineCal.time

        // If the last completion was already within this period, do nothing.
        if (lastCompletion != null && lastCompletion.after(periodStart) && lastCompletion.before(periodEnd)) {
            return this // Return the same object if already completed
        }

        // Calculate new streak
        val newStreakCount = if (lastCompletion == null) {
            1 // First completion
        } else {
            // Check if the last completion was in the immediately preceding period.
            val prevDeadlineCal = Calendar.getInstance()
            prevDeadlineCal.time = periodStart // Start from the beginning of the current period
            prevDeadlineCal.add(Calendar.DAY_OF_YEAR, -1)
            val prevPeriodStart = prevDeadlineCal.time

            if (lastCompletion.after(prevPeriodStart)) {
                this.streakCount + 1 // It was in the last period, so increment
            } else {
                1 // It was not in the last period, so reset to 1
            }
        }

        // Create the new updated habit object
        val newHabit = this.copy(
            completionDates = this.completionDates + now,
            completionCount = this.completionCount + 1,
            streakCount = newStreakCount,
            completed = true // This field might be redundant now but let's keep it for consistency
        )
        newHabit.id = this.id // Manually carry over the ID

        return newHabit
    }
}
