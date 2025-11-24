package com.example.habithero.data

import android.util.Log
import com.example.habithero.model.Category
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
            // Atomically increment the habit counters
            transaction.update(statsRef, "habitsCreated", FieldValue.increment(1))
            transaction.update(statsRef, "activeHabits", FieldValue.increment(1))
            // Use dot notation to increment a field within a map
            transaction.update(statsRef, "categoryCounts.${habit.category}", FieldValue.increment(1))

            // Add the new habit
            transaction.set(habitRef, habit)

            null // Firestore transactions require a return value
        }.await()
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
            // Decrement active habits
            transaction.update(statsRef, "activeHabits", FieldValue.increment(-1))
            // Delete habit
            transaction.delete(habitRef)
            null
        }.await()
    }

    private fun documentToHabit(document: DocumentSnapshot): Habit? {
        if (!document.exists()) return null
        return try {
            val completionDates = parseCompletionDates(document.get("completionDates"))
            val habit = Habit(
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
            )
            habit.id = document.id
            habit
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
