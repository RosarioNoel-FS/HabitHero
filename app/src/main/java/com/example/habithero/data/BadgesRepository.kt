package com.example.habithero.data

import com.example.habithero.model.UserBadge
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BadgesRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getUnlockedBadgeIds(userId: String): Set<String> {
        val snapshot = db.collection("users").document(userId).collection("badges").get().await()
        return snapshot.documents.map { it.id }.toSet()
    }

    suspend fun unlockBadge(userId: String, badgeId: String) {
        val userBadge = UserBadge(badgeId = badgeId)
        db.collection("users").document(userId).collection("badges").document(badgeId)
            .set(userBadge).await()
    }
}
