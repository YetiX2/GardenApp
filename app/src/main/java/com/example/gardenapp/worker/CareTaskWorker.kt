package com.example.gardenapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gardenapp.data.repo.GardenRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class CareTaskWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: GardenRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CareTaskWorker", "Worker starting...")
        return try {
            val allRules = repository.getAllCareRules() // MODIFIED THIS
            if (allRules.isEmpty()) {
                Log.d("CareTaskWorker", "No care rules found. Exiting.")
                return Result.success()
            }

            allRules.forEach { rule ->
                rule.everyDays?.let { days ->
                    val lastTask = repository.getLatestTaskForRule(rule.id)
                    val lastTaskDate = lastTask?.due?.toLocalDate() ?: rule.start
                    val nextTaskDate = lastTaskDate.plusDays(days.toLong())

                    if (!nextTaskDate.isAfter(LocalDate.now())) {
                        Log.d("CareTaskWorker", "Creating task for rule: ${rule.id}")
                        repository.createTaskFromRule(rule, nextTaskDate.atStartOfDay())
                    }
                }
                // TODO: Implement logic for monthly rules (everyMonths)
            }

            Log.d("CareTaskWorker", "Worker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("CareTaskWorker", "Worker failed", e)
            Result.failure()
        }
    }
}
