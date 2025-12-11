package com.example.gardenapp.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskWithPlantInfo

// TODO: Move to a shared file
private fun TaskStatus.toRussian(): String = when (this) {
    TaskStatus.PENDING -> "Новые"
    TaskStatus.DONE -> "Готово"
    TaskStatus.SNOOZED -> "Ждут"
    TaskStatus.REJECTED -> "Нафиг"
}

@Composable
fun TaskSummary(tasks: List<TaskWithPlantInfo>) {
    val counts = tasks.groupingBy { it.task.status }.eachCount()

    if (tasks.isEmpty()) {
        Text("Задач нет.", style = MaterialTheme.typography.bodyMedium)
    } else {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            TaskStatus.values().forEach { status ->
                val count = counts.getOrDefault(status, 0)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
                    Text(status.toRussian(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
