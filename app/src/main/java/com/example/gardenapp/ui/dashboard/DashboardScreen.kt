package com.example.gardenapp.ui.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.ui.dashboard.dialogs.AddFertilizerLogDialog
import com.example.gardenapp.ui.dashboard.dialogs.AddHarvestLogDialog
import com.example.gardenapp.ui.dashboard.dialogs.AddTaskDialog
import com.example.gardenapp.ui.dashboard.widgets.AdCard
import com.example.gardenapp.ui.dashboard.widgets.MyGardensCard
import com.example.gardenapp.ui.dashboard.widgets.RecentEntriesCard
import com.example.gardenapp.ui.dashboard.widgets.TodayTasksCard
import com.example.gardenapp.ui.dashboard.widgets.WeatherCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenGardens: () -> Unit,
    onOpenTasks: () -> Unit,
    vm: DashboardVm = hiltViewModel()
) {
    val allTasks by vm.allTasks.collectAsState(initial = emptyList())
    val gardens by vm.gardens.collectAsState(initial = emptyList())
    val recentActivity by vm.recentActivity.collectAsState(initial = emptyList())
    val allPlants by vm.allPlants.collectAsState(initial = emptyList())
    val weatherState by vm.weatherState.collectAsState()
    var showAddMenu by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddFertilizerDialog by remember { mutableStateOf(false) }
    var showAddHarvestDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            vm.fetchWeather()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }


    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { plant, type, due ->
                vm.addTask(plant, type, due)
                showAddTaskDialog = false
            },
            plants = allPlants
        )
    }
    if (showAddFertilizerDialog) {
        AddFertilizerLogDialog(
            onDismiss = { showAddFertilizerDialog = false },
            onAddLog = { plant, grams, date, note ->
                vm.addFertilizerLog(plant, grams, date, note)
                showAddFertilizerDialog = false
            },
            plants = allPlants
        )
    }

    if (showAddHarvestDialog) {
        AddHarvestLogDialog(
            onDismiss = { showAddHarvestDialog = false },
            onAddLog = { plant, weight, date, note ->
                vm.addHarvestLog(plant, weight, date, note)
                showAddHarvestDialog = false
            },
            plants = allPlants
        )
    }

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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMenu = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { WeatherCard(state = weatherState, onRetry = { vm.fetchWeather() }) }
            item { TodayTasksCard(tasks = allTasks, onOpenTasks = onOpenTasks) }
            item { MyGardensCard(gardens = gardens, onOpenGardens = onOpenGardens) }
            item { RecentEntriesCard(activityItems = recentActivity) }
            item { AdCard() }
        }

        if (showAddMenu) {
            ModalBottomSheet(
                onDismissRequest = { showAddMenu = false },
                sheetState = bottomSheetState
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    ListItem(
                        headlineContent = { Text("Добавить задачу") },
                        leadingContent = { Icon(Icons.Default.PlaylistAddCheck, null) },
                        modifier = Modifier.clickable {
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                showAddMenu = false
                                showAddTaskDialog = true
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Добавить запись об удобрении") },
                        leadingContent = { Icon(Icons.Default.Science, null) },
                        modifier = Modifier.clickable {
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                showAddMenu = false
                                showAddFertilizerDialog = true
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Добавить запись об урожае") },
                        leadingContent = { Icon(Icons.Default.Agriculture, null) },
                        modifier = Modifier.clickable {
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                showAddMenu = false
                                showAddHarvestDialog = true
                            }
                        }
                    )
                }
            }
        }
    }
}
