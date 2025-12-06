package com.example.habithero.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.habithero.ChallengeEventManager
import com.example.habithero.model.ChallengeEnrollment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class ChallengeEvaluationWorker(
    appContext: Context, 
    workerParams: WorkerParameters,
    private val challengeEventManager: ChallengeEventManager
    ) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return ListenableWorker.Result.failure()
        val db = FirebaseFirestore.getInstance()

        try {
            val enrollments = db.collection("users").document(userId)
                .collection("challengeEnrollments").whereEqualTo("status", "ACTIVE").get().await()

            for (enrollmentDoc in enrollments.documents) {
                val enrollment = enrollmentDoc.toObject(ChallengeEnrollment::class.java) ?: continue
                val challengeId = enrollment.challengeId

                val today = Calendar.getInstance()
                val lastEval = enrollment.lastEvaluatedDate?.let { date ->
                    Calendar.getInstance().apply { time = date }
                }

                if (lastEval != null && lastEval.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && lastEval.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    continue // Already evaluated today
                }

                val habits = db.collection("users").document(userId).collection("habits")
                    .whereEqualTo("sourceChallengeId", challengeId).get().await()

                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

                val allCompletedYesterday = habits.documents.isNotEmpty() && habits.documents.all { doc ->
                    doc.getTimestamp("lastCompletionDate")?.let { timestamp ->
                        val completionCal = Calendar.getInstance().apply { time = timestamp.toDate() }
                        completionCal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) && completionCal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
                    } ?: false
                }

                if (!allCompletedYesterday) {
                    val newLives = enrollment.lives - 1
                    if (newLives > 0) {
                        val updates = mapOf("lives" to newLives, "lastEvaluatedDate" to Date())
                        enrollmentDoc.reference.update(updates).await()
                        challengeEventManager.recordLifeLost(challengeId)
                    } else {
                        // Ran out of lives, fail the challenge and delete the habits in a batch write
                        db.runBatch {
                            batch ->
                            for(habitDoc in habits.documents) {
                                batch.delete(habitDoc.reference)
                            }
                            val updates = mapOf("lives" to 0, "status" to "FAILED", "lastEvaluatedDate" to Date())
                            batch.update(enrollmentDoc.reference, updates)
                        }.await()
                        challengeEventManager.recordLifeLost(challengeId)
                    }
                } else {
                     enrollmentDoc.reference.update(mapOf("lastEvaluatedDate" to Date())).await()
                }
            }
            return ListenableWorker.Result.success()
        } catch (e: Exception) {
            return ListenableWorker.Result.retry()
        }
    }
}
