package com.example.habithero

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    // --- Suspend Functions for Modern Architecture ---

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

    suspend fun addHabitSuspend(userId: String, habit: Habit): DocumentReference {
        return try {
            db.collection("users").document(userId).collection("habits").add(habit).await()
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error adding habit via suspend function", e)
            throw e
        }
    }

    suspend fun updateHabitSuspend(userId: String, habit: Habit) {
        try {
            db.collection("users").document(userId).collection("habits").document(habit.id).set(habit).await()
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error updating habit via suspend function", e)
            throw e
        }
    }

    suspend fun deleteHabit(userId: String, habitId: String) {
        try {
            db.collection("users").document(userId).collection("habits").document(habitId).delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error deleting habit: ", e)
            throw e // Re-throw to be handled by ViewModel
        }
    }

    suspend fun deleteHabitSuspend(userId: String, habitId: String) {
        try {
            db.collection("users").document(userId).collection("habits").document(habitId).delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error deleting habit via suspend function", e)
            throw e
        }
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
                completionCount = completionDates.size, // This is the fix
                completionDates = completionDates,
                reminderEnabled = document.getBoolean("reminderEnabled") ?: false,
                reminderTimeMinutes = (document.getLong("reminderTimeMinutes") ?: 15L).toInt(),
                sourceChallengeId = document.getString("sourceChallengeId"),
                sourceTemplateId = document.getString("sourceTemplateId"),
                // Deprecated fields, kept for Firestore compatibility
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
