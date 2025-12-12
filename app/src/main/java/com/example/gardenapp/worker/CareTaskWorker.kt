package com.example.gardenapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gardenapp.data.db.GardenDatabase
import com.example.gardenapp.data.repo.GardenRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

// NO @HiltWorker annotation
class CareTaskWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    // Manually create dependencies
    private val db = GardenDatabase.getDatabase(context) // We will add getDatabase static method
    private val repository = GardenRepository(db, db.referenceDao())

    override suspend fun doWork(): Result {
        Log.d("CareTaskWorker", "Worker starting...")
        return try {
            val allRules = repository.getAllCareRules()
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
            }

            Log.d("CareTaskWorker", "Worker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("CareTaskWorker", "Worker failed", e)
            Result.failure()
        }
    }
}
