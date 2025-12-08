package com.example.gardenapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.TaskWithPlantInfo

// Helper to get a Russian representation of the task type
private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onOpenGardens: () -> Unit, vm: DashboardVm = hiltViewModel()) {
    val tasks by vm.pendingTasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Сегодня на даче") }) },
        floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TodayTasksCard(tasks = tasks)
            }
            item {
                MyGardensCard(onOpenGardens = onOpenGardens)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayTasksCard(tasks: List<TaskWithPlantInfo>) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text("Сегодняшние задачи", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            if (tasks.isEmpty()) {
                Text("Задач на сегодня нет.", style = MaterialTheme.typography.bodyMedium)
            } else {
                tasks.take(2).forEach { taskInfo ->
                    Row(Modifier.fillMaxWidth()) {
                        Checkbox(checked = false, onCheckedChange = {})
                        val taskText = "${taskInfo.task.type.toRussian()} \"${taskInfo.plantName}\""
                        Text(taskText)
                    }
                }
                TextButton(onClick = { /* TODO */ }) { Text("Посмотреть все") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyGardensCard(onOpenGardens: () -> Unit) {
    Card(onClick = onOpenGardens) {
        Column(Modifier.padding(16.dp)) {
            Text("Мои грядки / участок", style = MaterialTheme.typography.titleLarge)
            // TODO: Add garden preview cards from the image
        }
    }
}
