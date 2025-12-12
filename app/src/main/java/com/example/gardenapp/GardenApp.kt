package com.example.gardenapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gardenapp.worker.CareTaskWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GardenApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val repeatingRequest = PeriodicWorkRequestBuilder<CareTaskWorker>(
            1, // repeatInterval
            TimeUnit.DAYS // timeUnit
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            CareTaskWorker::class.java.name,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
