package com.example.habithero.data

import android.util.Log
import com.example.habithero.model.Category
import com.example.habithero.model.Challenge
import com.example.habithero.model.Habit
import com.example.habithero.model.UserStats
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    suspend fun initializeUserStats(userId: String) {
        val statsRef = db.collection("users").document(userId).collection("stats").document("user_stats")
        try {
            val statsDoc = statsRef.get().await()
            if (!statsDoc.exists()) {
                statsRef.set(UserStats(), SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error initializing user stats", e)
        }
    }

    suspend fun getChallengeProgress(userId: String, challengeId: String): Pair<Challenge?, List<Habit>> {
        val challengeRef = db.collection("challenges").document(challengeId)
        val challenge = challengeRef.get().await().toObject(Challenge::class.java)

        val habits = fetchUserHabitsByChallenge(userId, challengeId)
        return Pair(challenge, habits)
    }

    private suspend fun fetchUserHabitsByChallenge(userId: String, challengeId: String): List<Habit> {
        return try {
            db.collection("users").document(userId).collection("habits")
                .whereEqualTo("sourceChallengeId", challengeId)
                .get()
                .await()
                .mapNotNull { document -> documentToHabit(document) }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching habits for challenge", e)
            emptyList()
        }
    }

    suspend fun fetchUserHabitsSuspend(userId: String): List<Habit> {
        return try {
            val task = db.collection("users").document(userId).collection("habits")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            task.mapNotNull { document -> documentToHabit(document) }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching habits via suspend function: ", e)
            throw e
        }
    }

    suspend fun getUserStats(userId: String): UserStats? {
        return try {
            val document = db.collection("users").document(userId).collection("stats").document("user_stats").get().await()
            document.toObject(UserStats::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching user stats: ", e)
            throw e
        }
    }

    suspend fun getHabitSuspend(userId: String, habitId: String): Habit? {
        return try {
            val document = db.collection("users").document(userId).collection("habits").document(habitId).get().await()
            documentToHabit(document)
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching single habit via suspend function: ", e)
            throw e
        }
    }

    suspend fun fetchCategoriesSuspend(): Map<String, Category> {
        return try {
            val task = db.collection("Category").get().await()
            task.documents.associate {
                val categoryName = it.getString("name") ?: ""
                @Suppress("UNCHECKED_CAST")
                val habitList = it.get("habit_list") as? List<String> ?: emptyList()
                val iconPath = it.getString("icon") ?: ""
                categoryName to Category(categoryName, habitList, iconPath)
            }.filterKeys { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error fetching categories via suspend function: ", e)
            throw e
        }
    }

    suspend fun addHabitAndUpdateStats(userId: String, habit: Habit) {
        val statsRef = db.collection("users").document(userId).collection("stats").document("user_stats")
        val habitRef = db.collection("users").document(userId).collection("habits").document()

        db.runTransaction { transaction ->
            transaction.update(statsRef, "habitsCreated", FieldValue.increment(1))
            transaction.update(statsRef, "activeHabits", FieldValue.increment(1))
            transaction.update(statsRef, "categoryCounts.${habit.category}", FieldValue.increment(1))
            transaction.set(habitRef, habit)
            null
        }.await()
    }

    suspend fun completeHabitAndUpdateStats(userId: String, habitId: String) {
        val habitRef = db.collection("users").document(userId).collection("habits").document(habitId)
        val statsRef = db.collection("users").document(userId).collection("stats").document("user_stats")

        db.runTransaction { transaction ->
            val habitSnapshot = transaction.get(habitRef)
            val statsSnapshot = transaction.get(statsRef)
            val habit = documentToHabit(habitSnapshot) ?: return@runTransaction

            val newCompletionDates = habit.completionDates + Date()
            val newStreak = habit.calculateStreak(newCompletionDates)

            // Update the habit with the new completion date
            transaction.update(habitRef, "completionDates", newCompletionDates)

            if (statsSnapshot.exists()) {
                // If stats exist, update them
                val currentBestStreak = statsSnapshot.getLong("bestStreak")?.toInt() ?: 0
                val updates = mutableMapOf<String, Any>()
                updates["totalCompleted"] = FieldValue.increment(1)
                updates["currentStreak"] = newStreak
                if (newStreak > currentBestStreak) {
                    updates["bestStreak"] = newStreak
                }
                transaction.update(statsRef, updates)
            } else {
                // If stats don't exist, create them with initial values
                val newStats = UserStats(
                    totalCompleted = 1,
                    currentStreak = newStreak,
                    bestStreak = newStreak,
                    // NOTE: This assumes a user completing a habit for the first time
                    // also has created at least one habit. This is a safe assumption.
                    habitsCreated = 1,
                    activeHabits = 1
                )
                transaction.set(statsRef, newStats)
            }
            null
        }.await()
    }


    suspend fun updateHabitReminder(userId: String, habitId: String, enabled: Boolean, minutes: Int) {
        val habitRef = db.collection("users").document(userId).collection("habits").document(habitId)
        val updates = if (enabled) mapOf("reminderEnabled" to true, "reminderTimeMinutes" to minutes) else mapOf("reminderEnabled" to false)
        habitRef.update(updates).await()
    }

    suspend fun detachHabitFromChallenge(userId: String, habitId: String) {
        val habitRef = db.collection("users").document(userId).collection("habits").document(habitId)
        val updates = mapOf("sourceChallengeId" to null, "sourceTemplateId" to null)
        habitRef.update(updates).await()
    }

    suspend fun updateHabitSuspend(userId: String, habit: Habit) {
        try {
            db.collection("users").document(userId).collection("habits").document(habit.id).set(habit).await()
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error updating habit via suspend function", e)
            throw e
        }
    }

    suspend fun deleteHabitSuspend(userId: String, habitId: String) {
        val statsRef = db.collection("users").document(userId).collection("stats").document("user_stats")
        val habitRef = db.collection("users").document(userId).collection("habits").document(habitId)

        db.runTransaction { transaction ->
            transaction.update(statsRef, "activeHabits", FieldValue.increment(-1))
            transaction.delete(habitRef)
            null
        }.await()
    }

    private fun documentToHabit(document: DocumentSnapshot): Habit? {
        if (!document.exists()) return null
        return try {
            val completionDates = parseCompletionDates(document.get("completionDates"))

            Habit(
                name = document.getString("name") ?: "",
                category = document.getString("category") ?: "",
                emoji = document.getString("emoji") ?: "",
                completionHour = (document.getLong("completionHour") ?: 21L).toInt(),
                completionMinute = (document.getLong("completionMinute") ?: 0L).toInt(),
                iconUrl = document.getString("iconUrl") ?: "",
                completionCount = completionDates.size,
                completionDates = completionDates,
                reminderEnabled = document.getBoolean("reminderEnabled") ?: false,
                reminderTimeMinutes = (document.getLong("reminderTimeMinutes") ?: 15L).toInt(),
                sourceChallengeId = document.getString("sourceChallengeId"),
                sourceTemplateId = document.getString("sourceTemplateId"),
                completed = document.getBoolean("completed") ?: false,
                timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
            ).apply {
                id = document.id
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error parsing habit ${document.id}", e)
            null
        }
    }

    private fun parseCompletionDates(data: Any?): List<Date> {
        if (data !is List<*>) return emptyList()
        val dates = mutableListOf<Date>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (item in data) {
            when (item) {
                is Timestamp -> dates.add(item.toDate())
                is String -> {
                    try {
                        sdf.parse(item)?.let { dates.add(it) }
                    } catch (e: ParseException) {
                        Log.e("FirebaseHelper", "Failed to parse old date format: $item", e)
                    }
                }
            }
        }
        return dates
    }
}
