package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskWithPlantInfo

// TODO: Move to a shared file
private fun TaskStatus.toRussian(): String = when (this) {
    TaskStatus.PENDING -> "Новые"
    TaskStatus.DONE -> "Готово"
    TaskStatus.SNOOZED -> "Ждут"
    TaskStatus.REJECTED -> "Нафиг"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksCard(tasks: List<TaskWithPlantInfo>, onOpenTasks: () -> Unit) {
    Card(onClick = onOpenTasks) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Сегодняшние задачи", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            val counts = tasks.groupingBy { it.task.status }.eachCount()

            if (counts.isEmpty()) {
                Text("Задач на сегодня нет.", style = MaterialTheme.typography.bodyMedium)
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
    }
}
