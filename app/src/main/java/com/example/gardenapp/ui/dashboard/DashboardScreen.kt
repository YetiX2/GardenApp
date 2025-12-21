package com.example.gardenapp.ui.dashboard

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.ui.dashboard.dialogs.AddFertilizerLogDialog
import com.example.gardenapp.ui.dashboard.dialogs.AddHarvestLogDialog
import com.example.gardenapp.ui.dashboard.dialogs.AddTaskDialog
import com.example.gardenapp.ui.dashboard.widgets.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenGardens: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenSettings: () -> Unit, // ADDED
    onOpenPlant: (String) -> Unit,
    vm: DashboardVm = hiltViewModel()
) {
    val allTasks by vm.allTasks.collectAsState(initial = emptyList())
    val gardens by vm.gardens.collectAsState(initial = emptyList())
    val recentActivity by vm.recentActivity.collectAsState(initial = emptyList())
    val allPlants by vm.allPlants.collectAsState(initial = emptyList())
    val weatherState by vm.weatherState.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(message = event.message)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> vm.onPermissionResult(isGranted) }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    var showAddMenu by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddFertilizerDialog by remember { mutableStateOf(false) }
    var showAddHarvestDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)




    // -- Pull to Refresh Logic (using the API compatible with your project) --
    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            vm.fetchWeather()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }


    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { plant, type, due, notes -> // MODIFIED
                vm.addTask(plant, type, due, notes) // MODIFIED
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

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }



    val settingsIntent = remember {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Сегодня на даче") },
                actions = {
                    IconButton(onClick = onOpenSettings) { // ADDED
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                    IconButton(onClick = { vm.runCareTaskWorkerNow() }) {
                        Icon(Icons.Default.BugReport, contentDescription = "Запустить CareTaskWorker")
                    }
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
        Box(modifier = Modifier.padding(pad).nestedScroll(pullToRefreshState.nestedScrollConnection)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { 
                    WeatherCard(
                        state = weatherState, 
                        onRetry = { vm.fetchWeather() }, 
                        onGrantPermission = { context.startActivity(settingsIntent) }
                    ) 
                }
                item { TodayTasksCard(tasks = allTasks, onOpenTasks = onOpenTasks) }
                item { MyGardensCard(gardens = gardens, onOpenGardens = onOpenGardens) }
                item { AdCard() }
                item { RecentEntriesCard(activityItems = recentActivity, onOpenPlant = onOpenPlant) }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState
            )
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
