package com.example.gardenapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gardenapp.R
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class CareTaskWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: GardenRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker starting with Hilt...")
        return try {
            val allRules = repository.getAllCareRules()
            if (allRules.isEmpty()) {
                Log.d(TAG, "No care rules found. Exiting.")
                return Result.success()
            }

            var tasksCreatedCount = 0
            allRules.forEach { rule ->
                rule.everyDays?.let { days ->
                    val lastTask = repository.getLatestTaskForRule(rule.id)
                    val lastTaskDate = lastTask?.due?.toLocalDate() ?: rule.start
                    val nextTaskDate = lastTaskDate.plusDays(days.toLong())

                    if (!nextTaskDate.isAfter(LocalDate.now())) {
                        Log.d(TAG, "Creating task for rule: ${rule.id}")
                        repository.createTaskFromRule(rule, nextTaskDate.atStartOfDay())
                        tasksCreatedCount++
                    }
                }
            }

            if (tasksCreatedCount > 0) {
                Log.d(TAG, "Created $tasksCreatedCount new tasks. Sending notification.")
                showNotification(tasksCreatedCount)
            }

            Log.d(TAG, "Worker finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed", e)
            Result.failure()
        }
    }

    private fun showNotification(taskCount: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Уведомления о задачах",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о новых задачах по уходу за садом"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Используем лого в качестве иконки
            .setContentTitle("Новые задачи по уходу")
            .setContentText("Появилось новых задач: $taskCount")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val TAG = "CareTaskWorker"
        private const val CHANNEL_ID = "garden_care_tasks_channel"
        private const val NOTIFICATION_ID = 1
    }
}
