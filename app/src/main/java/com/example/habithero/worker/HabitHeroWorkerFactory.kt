package com.example.habithero.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.habithero.ChallengeEventManager

class HabitHeroWorkerFactory(
    private val challengeEventManager: ChallengeEventManager
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            ChallengeEvaluationWorker::class.java.name -> {
                ChallengeEvaluationWorker(appContext, workerParameters, challengeEventManager)
            }
            else -> null
        }
    }
}
