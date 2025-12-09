package com.example.gardenapp.ui.plant

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.CareRuleEntity
import com.example.gardenapp.data.db.FertilizerLogEntity
import com.example.gardenapp.data.db.HarvestLogEntity
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.ui.dashboard.UiEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlantEditorScreen(onBack: () -> Unit, vm: PlantEditorVm = hiltViewModel()) {
    val plant by vm.plant.collectAsState()
    val fertilizerLogs by vm.fertilizerLogs.collectAsState(initial = emptyList())
    val harvestLogs by vm.harvestLogs.collectAsState(initial = emptyList())
    val careRules by vm.careRules.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(message = event.message)
            }
        }
    }

    val tabTitles = listOf("Инфо", "Удобрения", "Урожай", "Уход")
    val pagerState = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()

    var showAddFertilizerDialog by remember { mutableStateOf(false) }
    var showAddHarvestDialog by remember { mutableStateOf(false) }

    if (showAddFertilizerDialog) {
        AddFertilizerLogDialogForPlant(
            onDismiss = { showAddFertilizerDialog = false },
            onAddLog = { grams, date, note ->
                vm.addFertilizerLog(grams, date, note)
                showAddFertilizerDialog = false
            }
        )
    }

    if (showAddHarvestDialog) {
        AddHarvestLogDialogForPlant(
            onDismiss = { showAddHarvestDialog = false },
            onAddLog = { weight, date, note ->
                vm.addHarvestLog(weight, date, note)
                showAddHarvestDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(plant?.title ?: "Загрузка...") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(state = pagerState) {
                page ->
                when (page) {
                    0 -> InfoTab(plant = plant)
                    1 -> FertilizerLogTab(logs = fertilizerLogs, onAdd = { showAddFertilizerDialog = true }, onDelete = { vm.deleteFertilizerLog(it) })
                    2 -> HarvestLogTab(logs = harvestLogs, onAdd = { showAddHarvestDialog = true }, onDelete = { vm.deleteHarvestLog(it) })
                    3 -> CareRulesTab(rules = careRules, onAdd = { /* TODO */ }, onDelete = { vm.deleteCareRule(it) })
                }
            }
        }
    }
}

@Composable
private fun InfoTab(plant: PlantEntity?) {
    if (plant == null) {
        // Show loading
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Сорт: ${plant.variety}")
            Text("Посажен: ${plant.plantedAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}")
        }
    }
}

@Composable
private fun FertilizerLogTab(logs: List<FertilizerLogEntity>, onAdd: () -> Unit, onDelete: (FertilizerLogEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) {
        padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            if (logs.isEmpty()) item { Text("Записей об удобрении пока нет.") }
            items(logs, key = { it.id }) { log ->
                ListItem(
                    headlineContent = { Text("${log.amountGrams}г - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                    supportingContent = { log.note?.let { Text(it) } },
                    trailingContent = { IconButton(onClick = { onDelete(log) }) { Icon(Icons.Default.Delete, null) } }
                )
            }
        }
    }
}

@Composable
private fun HarvestLogTab(logs: List<HarvestLogEntity>, onAdd: () -> Unit, onDelete: (HarvestLogEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) {
        padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            if (logs.isEmpty()) item { Text("Записей об урожае пока нет.") }
            items(logs, key = { it.id }) { log ->
                ListItem(
                    headlineContent = { Text("${log.weightKg}кг - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                    supportingContent = { log.note?.let { Text(it) } },
                    trailingContent = { IconButton(onClick = { onDelete(log) }) { Icon(Icons.Default.Delete, null) } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFertilizerLogDialogForPlant(onDismiss: () -> Unit, onAddLog: (grams: Float, date: LocalDate, note: String?) -> Unit) { /* ... */ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHarvestLogDialogForPlant(
    onDismiss: () -> Unit,
    onAddLog: (weight: Float, date: LocalDate, note: String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) { /* Date Picker Dialog */ }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись об урожае") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Вес (кг)") })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка (необязательно)") })
                Row(verticalAlignment = Alignment.CenterVertically) { /* Date Picker Row */ }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountFloat = amount.toFloatOrNull()
                    if (amountFloat != null) {
                        onAddLog(amountFloat, selectedDate, note.ifBlank { null })
                    }
                },
                enabled = amount.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

