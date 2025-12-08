package com.example.gardenapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
fun DashboardScreen(onOpenGardens: () -> Unit, vm: DashboardVm = hiltViewModel()) {
    val tasks by vm.pendingTasks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Сегодня на даче") },
                actions = {
                    IconButton(onClick = { vm.createTestData() }) {
                        Icon(Icons.Default.Science, contentDescription = "Заполнить тестовыми данными")
                    }
                }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, null) } }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeatherCard()
            }
            item {
                TodayTasksCard(tasks = tasks)
            }
            item {
                MyGardensCard(onOpenGardens = onOpenGardens)
            }
        }
    }
}

@Composable
private fun WeatherCard() {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.WbSunny, contentDescription = "Погода", modifier = Modifier.size(64.dp), tint = Color(0xFFFFC107))
            Spacer(Modifier.width(16.dp))
            Text("23°", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Ночью возможны заморозки до -2 °C", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ForecastItem(day = "Пт", temp = "25°", icon = Icons.Outlined.WbSunny)
                    ForecastItem(day = "Сб", temp = "21°", icon = Icons.Outlined.Thermostat) // Placeholder icon
                    ForecastItem(day = "Вс", temp = "16°", icon = Icons.Outlined.Thermostat) // Placeholder icon
                }
            }
        }
    }
}

@Composable
private fun ForecastItem(day: String, temp: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, style = MaterialTheme.typography.bodySmall)
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(temp, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
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
                        val taskDescription = taskInfo.task.type.toRussian()
                        val taskText = "$taskDescription \"${taskInfo.plantName}\""
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
