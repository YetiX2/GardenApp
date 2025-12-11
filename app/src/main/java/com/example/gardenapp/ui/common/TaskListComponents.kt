package com.example.gardenapp.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.TaskWithPlantInfo

// TODO: Move to a shared file
private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.OTHER -> "Другое"
}

@Composable
internal fun TaskList(
    tasks: List<TaskWithPlantInfo>,
    onStatusChange: (String, TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (tasks.isEmpty()) {
            item { Text("Задач в этом статусе нет.", modifier = Modifier.padding(16.dp)) }
        } else {
            items(tasks, key = { it.task.id }) { taskInfo ->
                TaskItem(
                    taskInfo = taskInfo,
                    onStatusChange = { newStatus -> onStatusChange(taskInfo.task.id, newStatus) }
                )
            }
        }
    }
}

@Composable
internal fun TaskItem(taskInfo: TaskWithPlantInfo, onStatusChange: (TaskStatus) -> Unit) {
    val taskDescription = taskInfo.task.type.toRussian()
    val taskText = "$taskDescription \"${taskInfo.plantName}\""

    ListItem(
        headlineContent = { Text(taskText) },
        supportingContent = { 
            // Show note if it exists
            taskInfo.task.notes?.let { Text(it) } 
        },
        trailingContent = {
            Row {
                if (taskInfo.task.status != TaskStatus.DONE) {
                    IconButton(onClick = { onStatusChange(TaskStatus.DONE) }) {
                        Icon(Icons.Default.Check, contentDescription = "Выполнено", tint = Color.Green)
                    }
                } else {
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Check, contentDescription = "Выполнено")
                    }
                }
                if (taskInfo.task.status != TaskStatus.SNOOZED) {
                    IconButton(onClick = { onStatusChange(TaskStatus.SNOOZED) }) {
                        Icon(Icons.Default.Pause, contentDescription = "Отложить", tint = Color(0xFFE69A1B)) // Amber
                    }
                } else {
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Pause, contentDescription = "Отложено")
                    }
                }
                if (taskInfo.task.status != TaskStatus.REJECTED) {
                    IconButton(onClick = { onStatusChange(TaskStatus.REJECTED) }) {
                        Icon(Icons.Default.Close, contentDescription = "Отклонить", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Close, contentDescription = "Отклонено")
                    }
                }
            }
        }
    )
    Divider()
}
