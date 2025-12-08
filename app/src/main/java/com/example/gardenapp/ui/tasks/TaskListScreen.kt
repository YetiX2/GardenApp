package com.example.gardenapp.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

private fun TaskStatus.toRussian(): String = when (this) {
    TaskStatus.PENDING -> "Новые"
    TaskStatus.DONE -> "Готово"
    TaskStatus.SNOOZED -> "Ждёт"
    TaskStatus.REJECTED -> "Нафиг"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(onBack: () -> Unit, vm: TaskListVm = hiltViewModel()) {
    val allTasks by vm.allTasks.collectAsState(initial = emptyList())
    var selectedTabIndex by remember { mutableStateOf(0) }

    val taskStatuses = listOf(TaskStatus.PENDING, TaskStatus.DONE, TaskStatus.SNOOZED, TaskStatus.REJECTED)

    val tasksToShow = remember(selectedTabIndex, allTasks) {
        allTasks.filter { it.task.status == taskStatuses[selectedTabIndex] }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все задачи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                taskStatuses.forEachIndexed { index, status ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(status.toRussian()) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (tasksToShow.isEmpty()) {
                    item {
                        Text("Задач в этом статусе нет.", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(tasksToShow, key = { it.task.id }) { taskInfo ->
                        TaskItem(
                            taskInfo = taskInfo,
                            onStatusChange = { newStatus -> vm.updateTaskStatus(taskInfo.task.id, newStatus) }
                        )
                    }
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
                // Show buttons only if the task is not already in that state
                if (taskInfo.task.status != TaskStatus.DONE) {
                    IconButton(onClick = { onStatusChange(TaskStatus.DONE) }) {
                        Icon(Icons.Default.Check, contentDescription = "Выполнено", tint = Color.Green)
                    }
                }else{
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Check, contentDescription = "Выполнено")
                    }
                }
                if (taskInfo.task.status != TaskStatus.SNOOZED) {
                    IconButton(onClick = { onStatusChange(TaskStatus.SNOOZED) }) {
                        Icon(Icons.Default.Pause, contentDescription = "Отложить", tint = Color(0xFFE69A1B)) // Amber
                    }
                }else{
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Pause, contentDescription = "Отложено")
                    }
                }
                if (taskInfo.task.status != TaskStatus.REJECTED) {
                    IconButton(onClick = { onStatusChange(TaskStatus.REJECTED) }) {
                        Icon(Icons.Default.Close, contentDescription = "Отклонить", tint = MaterialTheme.colorScheme.error)
                    }
                }else{
                    IconButton(onClick = { }, enabled = false) {
                        Icon(Icons.Default.Close, contentDescription = "Отклонено")
                    }
                }
            }
        }
    )
    Divider()
}
