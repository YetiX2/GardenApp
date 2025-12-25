package com.example.gardenapp.ui.tasks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.ui.common.TaskList
import com.example.gardenapp.ui.common.dialogs.UpsertTaskDialog
import com.example.gardenapp.ui.dashboard.UiEvent
import com.example.gardenapp.ui.dashboard.dialogs.AddHarvestLogDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private fun TaskStatus.toRussian(): String = when (this) {
    TaskStatus.PENDING -> "Новые"
    TaskStatus.DONE -> "Готово"
    TaskStatus.SNOOZED -> "Ждёт"
    TaskStatus.REJECTED -> "Нафиг"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(onBack: () -> Unit, vm: TaskListVm = hiltViewModel()) {
    val allTasks by vm.allTasks.collectAsState(initial = emptyList())
    val allPlants by vm.allPlants.collectAsState(initial = emptyList())
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val taskToConfirmHarvest by vm.taskToConfirmHarvest.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(message = event.message)
            }
        }
    }

    val taskStatuses = listOf(TaskStatus.PENDING, TaskStatus.DONE, TaskStatus.SNOOZED, TaskStatus.REJECTED)
    val pagerState = rememberPagerState { taskStatuses.size }
    val scope = rememberCoroutineScope()

    if (showAddTaskDialog) {
        UpsertTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { plant, type, due, notes, amount, unit ->
                vm.addTask(plant, type, due, notes, amount, unit)
                showAddTaskDialog = false
            },
            plants = allPlants
        )
    }

    taskToConfirmHarvest?.let {
        AddHarvestLogDialog(
            onDismiss = { vm.dismissHarvestConfirmation() },
            onAddLog = { _, weight, date, note -> // plant is not needed here
                vm.confirmHarvestAndCompleteTask(it.id, it.plantId, weight, date, note)
            },
            plants = allPlants.filter { p -> p.id == it.plantId } // Pass only the relevant plant
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Все задачи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                taskStatuses.forEachIndexed { index, status ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(status.toRussian()) }
                    )
                }
            }
            HorizontalPager(state = pagerState) {
                pageIndex ->
                val tasksToShow = allTasks.filter { it.task.status == taskStatuses[pageIndex] }
                TaskList(tasks = tasksToShow, onStatusChange = vm::updateTaskStatus)
            }
        }
    }
}
