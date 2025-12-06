package com.example.habithero

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.habithero.worker.ChallengeEvaluationWorker
import com.example.habithero.worker.HabitHeroWorkerFactory
import com.jakewharton.threetenabp.AndroidThreeTen
import java.util.concurrent.TimeUnit

class HabitHeroApplication : Application(), Configuration.Provider {

    lateinit var challengeEventManager: ChallengeEventManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(HabitHeroWorkerFactory(challengeEventManager))
            .build()

    override fun onCreate() {
        super.onCreate()
        challengeEventManager = ChallengeEventManager(this)
        AndroidThreeTen.init(this) // Initialize the ThreeTenABP library
        setupDailyChallengeWorker()
    }

    private fun setupDailyChallengeWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ChallengeEvaluationWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ChallengeEvaluationWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
