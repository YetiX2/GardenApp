package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskWithPlantInfo
import java.time.format.DateTimeFormatter

private fun com.example.gardenapp.data.db.TaskType.toRussian(): String = when (this) {
    com.example.gardenapp.data.db.TaskType.FERTILIZE -> "Подкормить"
    com.example.gardenapp.data.db.TaskType.PRUNE -> "Обрезать"
    com.example.gardenapp.data.db.TaskType.TREAT -> "Обработать"
    com.example.gardenapp.data.db.TaskType.WATER -> "Полить"
    com.example.gardenapp.data.db.TaskType.OTHER -> "Другое"
}

@Composable
fun TasksTab(tasks: List<TaskWithPlantInfo>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Task List ---
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (tasks.isEmpty()) {
                item { Text("Задач для этого растения нет.") }
            } else {
                items(tasks, key = { it.task.id }) { taskInfo ->
                    val formattedDate = taskInfo.task.due.format(DateTimeFormatter.ofPattern("dd MMMM, HH:mm"))
                    ListItem(
                        headlineContent = { Text("${taskInfo.task.type.toRussian()} - ${taskInfo.task.status}") },
                        supportingContent = { Text("Срок: $formattedDate") }
                    )
                }
            }
        }

        // --- Ad Block ---
        Card(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Все для сада и огорода") },
                supportingContent = { Text("в аффилированном магазине") },
                trailingContent = { Icon(Icons.Outlined.Link, null) }
            )
        }
    }
}
