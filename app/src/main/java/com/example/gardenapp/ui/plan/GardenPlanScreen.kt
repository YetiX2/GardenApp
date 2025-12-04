package com.example.gardenapp.ui.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlin.math.hypot
import kotlin.math.round

@HiltViewModel
class PlanVm @Inject constructor(private val repo: GardenRepository) : androidx.lifecycle.ViewModel() {
    fun plantsFlow(gardenId: String): Flow<List<PlantEntity>> = repo.plants(gardenId)
    fun fertilizerLogsFlow(plantId: String): Flow<List<FertilizerLogEntity>> = repo.fertilizerLogs(plantId)
    fun harvestLogsFlow(plantId: String): Flow<List<HarvestLogEntity>> = repo.harvestLogs(plantId)
    fun careRulesFlow(plantId: String): Flow<List<CareRuleEntity>> = repo.careRules(plantId)

    suspend fun upsertPlant(p: PlantEntity) = repo.upsertPlant(p)
    suspend fun deletePlant(p: PlantEntity) = repo.deletePlant(p)
    suspend fun addFertilizer(plantId: String, date: LocalDate, grams: Float, note: String?) =
        repo.addFertilizerLog(plantId, date, grams, note)

    suspend fun deleteFertilizer(item: FertilizerLogEntity) = repo.deleteFertilizerLog(item)
    suspend fun addHarvest(plantId: String, date: LocalDate, kg: Float, note: String?) = repo.addHarvestLog(plantId, date, kg, note)
    suspend fun deleteHarvest(item: HarvestLogEntity) = repo.deleteHarvestLog(item)
    suspend fun addCareRule(plantId: String, type: TaskType, start: LocalDate, everyDays: Int?, everyMonths: Int?) =
        repo.addCareRule(plantId, type, start, everyDays, everyMonths)
    suspend fun deleteCareRule(rule: CareRuleEntity) = repo.deleteCareRule(rule)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenPlanScreen(gardenId: String, onBack: () -> Unit, vm: PlanVm = hiltViewModel()) {
    val scope = rememberCoroutineScope()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var snapToGrid by remember { mutableStateOf(true) }

    var plants by remember { mutableStateOf(listOf<PlantEntity>()) }
    LaunchedEffect(gardenId) { vm.plantsFlow(gardenId).collectLatest { plants = it } }

    var selectedPlant by remember { mutableStateOf<PlantEntity?>(null) }
    var dragging by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }

    val baseGridPx = 50f
    fun screenToWorld(p: Offset): Offset = (p - offset) / scale
    fun worldToScreen(p: Offset): Offset = p * scale + offset
    fun snap(p: Offset): Offset = if (!snapToGrid) p else Offset(
        x = (round(p.x / baseGridPx) * baseGridPx),
        y = (round(p.y / baseGridPx) * baseGridPx)
    )

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("План сада") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton({ snapToGrid = !snapToGrid }) { Icon(Icons.Outlined.GridOn, contentDescription = null) }
                    IconButton({ scale = 1f; offset = Offset.Zero }) { Icon(Icons.Outlined.CenterFocusStrong, contentDescription = null) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val centerWorld = screenToWorld(Offset(600f, 900f))
                val newPlant = PlantEntity(
                    id = UUID.randomUUID().toString(),
                    gardenId = gardenId,
                    title = "Новое растение",
                    variety = null,
                    x = centerWorld.x,
                    y = centerWorld.y,
                    radius = 35f,
                    plantedAt = LocalDate.now()
                )
                selectedPlant = newPlant
                showEditor = true
            }) { Icon(Icons.Default.Add, contentDescription = null) }
        }
    ) { pad ->
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        val plantColor = Color(0xFF4CAF50)
        val selectedStroke = MaterialTheme.colorScheme.primary

        Box(Modifier.fillMaxSize().padding(pad)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { // Use Unit to avoid recompositions
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (!dragging) {
                                scale = (scale * zoom).coerceIn(0.5f, 6f)
                                offset += pan
                            }
                        }
                    }
                    .pointerInput(plants, selectedPlant, dragging, snapToGrid, scale, offset) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.first()

                                if (change.pressed && !change.previousPressed) {
                                    val world = screenToWorld(change.position)
                                    val hit = plants.minByOrNull { hypot(it.x - world.x, it.y - world.y) - it.radius }
                                    val hitOk = hit != null && hypot(hit.x - world.x, hit.y - world.y) <= hit.radius + 16
                                    if (hitOk) {
                                        selectedPlant = hit!!
                                        if (event.keyboardModifiers.isCtrlPressed) {
                                            showEditor = true
                                        }
                                        dragging = true
                                    } else {
                                        selectedPlant = null
                                    }
                                }

                                if (dragging && selectedPlant != null) {
                                    if (change.pressed) {
                                        val world = screenToWorld(change.position)
                                        val newWorld = if (snapToGrid) snap(world) else world
                                        val current = plants.firstOrNull { it.id == selectedPlant?.id }
                                        if (current != null) {
                                            scope.launch { vm.upsertPlant(current.copy(x = newWorld.x, y = newWorld.y)) }
                                        }
                                    } else {
                                        dragging = false
                                    }
                                }
                            }
                        }
                    }
            ) {
                val step = baseGridPx * scale
                var x = (-offset.x % step)
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height))
                    x += step
                }
                var y = (-offset.y % step)
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y))
                    y += step
                }
                plants.forEach { p ->
                    val center = worldToScreen(Offset(p.x, p.y))
                    drawCircle(color = plantColor, radius = p.radius * scale, center = center)
                    if (p.id == selectedPlant?.id) {
                        drawCircle(
                            color = selectedStroke,
                            radius = (p.radius + 6) * scale,
                            center = center,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }

            val current = selectedPlant
            if (current != null) {
                ActionBarForPlant(
                    plant = current,
                    onRadiusMinus = { scope.launch { vm.upsertPlant(current.copy(radius = (current.radius - 5f).coerceAtLeast(10f))) } },
                    onRadiusPlus = { scope.launch { vm.upsertPlant(current.copy(radius = (current.radius + 5f).coerceAtMost(300f))) } },
                    onDelete = { scope.launch { vm.deletePlant(current) }; selectedPlant = null },
                    onEdit = { showEditor = true }
                )
            }

            if (showEditor && current != null) {
                ModalBottomSheet(onDismissRequest = { showEditor = false }, sheetState = bottomSheetState) {
                    PlantEditor(
                        plant = current,
                        fertilizerFlow = vm.fertilizerLogsFlow(current.id),
                        harvestFlow = vm.harvestLogsFlow(current.id),
                        careRulesFlow = vm.careRulesFlow(current.id),
                        onSave = { updated -> 
                            scope.launch { vm.upsertPlant(updated) }
                            showEditor = false 
                        },
                        onCancel = { showEditor = false }, // Added cancel logic
                        onAddFertilizer = { date, grams, note -> scope.launch { vm.addFertilizer(current.id, date, grams, note) } },
                        onDeleteFertilizer = { item -> scope.launch { vm.deleteFertilizer(item) } },
                        onAddHarvest = { date, kg, note -> scope.launch { vm.addHarvest(current.id, date, kg, note) } },
                        onDeleteHarvest = { item -> scope.launch { vm.deleteHarvest(item) } },
                        onAddCareRule = { type, start, everyDays, everyMonths -> scope.launch { vm.addCareRule(current.id, type, start, everyDays, everyMonths) } },
                        onDeleteCareRule = { rule -> scope.launch { vm.deleteCareRule(rule) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionBarForPlant(
    plant: PlantEntity,
    onRadiusMinus: () -> Unit,
    onRadiusPlus: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(plant.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            FilledTonalIconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
            FilledTonalIconButton(onClick = onRadiusMinus) { Icon(Icons.Default.RemoveCircle, contentDescription = null) }
            FilledTonalIconButton(onClick = onRadiusPlus) { Icon(Icons.Default.Add, contentDescription = null) }
            FilledTonalIconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null) }
        }
    }
}

@Composable
private fun PlantEditor(
    plant: PlantEntity,
    harvestFlow: Flow<List<HarvestLogEntity>>,
    careRulesFlow: Flow<List<CareRuleEntity>>,
    onSave: (PlantEntity) -> Unit,
    onCancel: () -> Unit, // Added onCancel
    onAddFertilizer: (LocalDate, Float, String?) -> Unit,
    onDeleteFertilizer: (FertilizerLogEntity) -> Unit,
    onAddHarvest: (LocalDate, Float, String?) -> Unit,
    onDeleteHarvest: (HarvestLogEntity) -> Unit,
    onAddCareRule: (TaskType, LocalDate, Int?, Int?) -> Unit,
    onDeleteCareRule: (CareRuleEntity) -> Unit,
    fertilizerFlow: Flow<List<FertilizerLogEntity>>
) {
    var title by remember { mutableStateOf(plant.title) }
    var variety by remember { mutableStateOf(plant.variety ?: "") }
    var plantedAt by remember { mutableStateOf(plant.plantedAt) }

    val fertilizer by fertilizerFlow.collectAsState(initial = emptyList())
    val harvest by harvestFlow.collectAsState(initial = emptyList())
    val careRules by careRulesFlow.collectAsState(initial = emptyList())

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Свойства растения", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = variety, onValueChange = { variety = it }, label = { Text("Сорт") }, modifier = Modifier.fillMaxWidth())
        DateRow(label = "Дата посадки", date = plantedAt, onPick = { plantedAt = it })
        Row {
            Button(onClick = { onSave(plant.copy(title = title, variety = variety.ifBlank { null }, plantedAt = plantedAt)) }) {
                Text("Сохранить")
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onCancel) { // Added Cancel button
                Text("Отмена")
            }
        }

        Divider()
        Text("Правила ухода", style = MaterialTheme.typography.titleMedium)
        AddCareRuleRow(onAdd = onAddCareRule)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(careRules, key = { it.id }) { rule ->
                ListItem(
                    headlineContent = { Text(rule.type.toString()) }, // Placeholder
                    supportingContent = { Text("Начиная с ${rule.start}, каждые ${rule.everyDays ?: rule.everyMonths} дней/месяцев") },
                    trailingContent = { IconButton(onClick = { onDeleteCareRule(rule) }) { Icon(Icons.Default.Delete, contentDescription = null) } }
                )
                Divider()
            }
        }

        Divider()
        Text("Внесение удобрений", style = MaterialTheme.typography.titleMedium)
        AddFertilizerRow(onAdd = onAddFertilizer)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(fertilizer, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text("${item.amountGrams} г") },
                    supportingContent = { Text("${item.date}") },
                    trailingContent = { IconButton(onClick = { onDeleteFertilizer(item) }) { Icon(Icons.Default.Delete, contentDescription = null) } }
                )
                Divider()
            }
        }

        Divider()
        Text("Урожай", style = MaterialTheme.typography.titleMedium)
        AddHarvestRow(onAdd = onAddHarvest)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(harvest, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text("${item.weightKg} кг") },
                    supportingContent = { Text("${item.date}") },
                    trailingContent = { IconButton(onClick = { onDeleteHarvest(item) }) { Icon(Icons.Default.Delete, contentDescription = null) } }
                )
                Divider()
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRow(label: String, date: LocalDate, onPick: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = date.toString(), onValueChange = {}, enabled = false, label = { Text(label) })
        FilledTonalButton(onClick = { show = true }) { Icon(Icons.Default.DateRange, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Выбрать") }
    }
    if (show) {
        DatePickerDialog(onDismissRequest = { show = false }, confirmButton = {}) {
            val state = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * 86_400_000)
            LaunchedEffect(state.selectedDateMillis) {
                val ms = state.selectedDateMillis ?: return@LaunchedEffect
                onPick(LocalDate.ofEpochDay(ms / 86_400_000))
            }
            DatePicker(state = state)
        }
    }
}

@Composable
private fun AddCareRuleRow(onAdd: (TaskType, LocalDate, Int?, Int?) -> Unit) {
    var type by remember { mutableStateOf(TaskType.WATER) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var every by remember { mutableStateOf("7") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // TODO: Add UI for selecting TaskType, start date, and interval
        Button(onClick = { onAdd(type, startDate, every.toIntOrNull(), null) }) { Text("Добавить правило") }
    }
}

@Composable
private fun AddFertilizerRow(onAdd: (LocalDate, Float, String?) -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var grams by remember { mutableStateOf("100") }
    var note by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateRow(label = "Дата", date = date, onPick = { date = it })
        OutlinedTextField(value = grams, onValueChange = { grams = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Количество (г)") })
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка (опц.)") })
        Button(onClick = { onAdd(date, grams.toFloatOrNull() ?: 0f, note.ifBlank { null }) }) { Text("Добавить запись") }
    }
}

@Composable
private fun AddHarvestRow(onAdd: (LocalDate, Float, String?) -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var kg by remember { mutableStateOf("1.0") }
    var note by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateRow(label = "Дата", date = date, onPick = { date = it })
        OutlinedTextField(value = kg, onValueChange = { kg = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Вес (кг)") })
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка (опц.)") })
        Button(onClick = { onAdd(date, kg.toFloatOrNull() ?: 0f, note.ifBlank { null }) }) { Text("Добавить запись") }
    }
}