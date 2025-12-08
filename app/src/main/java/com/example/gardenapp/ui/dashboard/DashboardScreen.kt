package com.example.gardenapp.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Info
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
import com.example.gardenapp.data.db.GardenEntity
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
    TaskStatus.SNOOZED -> "Ждут"
    TaskStatus.REJECTED -> "Нафиг"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenGardens: () -> Unit,
    onOpenTasks: () -> Unit,
    vm: DashboardVm = hiltViewModel()
) {
    val allTasks by vm.allTasks.collectAsState(initial = emptyList())
    val gardens by vm.gardens.collectAsState(initial = emptyList())

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
                TodayTasksCard(tasks = allTasks, onOpenTasks = onOpenTasks)
            }
            item {
                MyGardensCard(gardens = gardens, onOpenGardens = onOpenGardens)
            }
            item {
                RecentEntriesCard()
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
private fun TodayTasksCard(tasks: List<TaskWithPlantInfo>, onOpenTasks: () -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyGardensCard(gardens: List<GardenEntity>, onOpenGardens: () -> Unit) {
    Column {
        Text("Мои грядки / участок", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Card(onClick = onOpenGardens) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                gardens.take(2).forEach { garden ->
                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(garden.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentEntriesCard() {
    Column {
        Text("Последние записи", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Появилась тля на смородине", style = MaterialTheme.typography.titleSmall)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(8.dp))
                    Text("Семена томатов под ваш регион", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}