package com.example.gardenapp.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.TaskWithPlantInfo

private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(onBack: () -> Unit, vm: TaskListVm = hiltViewModel()) {
    val tasks by vm.pendingTasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сегодняшние задачи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tasks.isEmpty()) {
                item {
                    Text("Задач на сегодня нет.", modifier = Modifier.padding(16.dp))
                }
            } else {
                items(tasks, key = { it.task.id }) { taskInfo ->
                    TaskItem(
                        taskInfo = taskInfo,
                        onStatusChange = { newStatus -> vm.updateTaskStatus(taskInfo.task.id, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(taskInfo: TaskWithPlantInfo, onStatusChange: (TaskStatus) -> Unit) {
    val taskDescription = taskInfo.task.type.toRussian()
    val taskText = "$taskDescription \"${taskInfo.plantName}\""
    
    ListItem(
        headlineContent = { Text(taskText) },
        trailingContent = {
            Row {
                IconButton(onClick = { onStatusChange(TaskStatus.DONE) }) {
                    Icon(Icons.Default.Check, contentDescription = "Выполнено", tint = Color.Green)
                }
                IconButton(onClick = { onStatusChange(TaskStatus.SNOOZED) }) {
                    Icon(Icons.Default.Pause, contentDescription = "Отложить", tint = Color(0xFFE69A1B)) // Amber
                }
                IconButton(onClick = { onStatusChange(TaskStatus.REJECTED) }) {
                    Icon(Icons.Default.Close, contentDescription = "Отклонить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
    Divider()
}
