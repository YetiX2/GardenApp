package com.example.gardenapp.ui.plan

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.GridOff
import androidx.compose.material.icons.outlined.GridOn
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
import com.example.gardenapp.data.db.PlantEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import kotlin.math.hypot
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenPlanScreen(gardenId: String, onBack: () -> Unit, vm: PlanVm = hiltViewModel()) {
    LaunchedEffect(gardenId) { vm.loadGarden(gardenId) }

    val scope = rememberCoroutineScope()
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var snapToGrid by remember { mutableStateOf(true) }
    var plants by remember { mutableStateOf(listOf<PlantEntity>()) }
    LaunchedEffect(gardenId) { vm.plantsFlow(gardenId).collectLatest { plants = it } }

    var selectedPlant by remember { mutableStateOf<PlantEntity?>(null) }
    var dragging by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }

    LaunchedEffect(plants) {
        selectedPlant?.let { currentSelected ->
            selectedPlant = plants.find { it.id == currentSelected.id }
        }
    }

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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Назад") } },
                actions = {
                    IconButton({ snapToGrid = !snapToGrid }) {
                        Icon(
                            if (snapToGrid) Icons.Outlined.GridOn else Icons.Outlined.GridOff,
                            contentDescription = "Привязка к сетке"
                        )
                    }
                    IconButton({ scale = 1f; offset = Offset.Zero }) { Icon(Icons.Outlined.CenterFocusStrong, contentDescription = "Сбросить вид") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val centerWorld = screenToWorld(Offset(600f, 900f))
                val newPlant = PlantEntity(
                    id = UUID.randomUUID().toString(),
                    gardenId = gardenId,
                    title = "",
                    variety = null,
                    varietyId = null,
                    x = centerWorld.x,
                    y = centerWorld.y,
                    radius = 35f,
                    plantedAt = LocalDate.now()
                )
                Log.d("GardenPlanScreen", "Created new plant: $newPlant")
                selectedPlant = newPlant
                isCreating = true
                showEditor = true
            }) { Icon(Icons.Default.Add, contentDescription = "Добавить") }
        }
    ) { pad ->
        val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        val plantColor = Color(0xFF4CAF50)
        val selectedStroke = MaterialTheme.colorScheme.primary

        Box(Modifier.fillMaxSize().padding(pad)) {
            Canvas(
                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (!dragging) {
                            scale = (scale * zoom).coerceIn(0.5f, 6f)
                            offset += pan
                        }
                    }
                }.pointerInput(plants, selectedPlant, dragging, snapToGrid, scale, offset) {
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
                                    isCreating = false
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
                if (snapToGrid) {
                    val step = baseGridPx * scale
                    var x = (-offset.x % step)
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height)); x += step
                    }
                    var y = (-offset.y % step)
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y)); y += step
                    }
                }
                plants.forEach { p ->
                    val center = worldToScreen(Offset(p.x, p.y))
                    drawCircle(color = plantColor, radius = p.radius * scale, center = center)
                    if (p.id == selectedPlant?.id && !isCreating) {
                        drawCircle(color = selectedStroke, radius = (p.radius + 6) * scale, center = center, style = Stroke(width = 3f))
                    }
                }
            }

            val current = selectedPlant
            if (current != null && !isCreating) {
                ActionBarForPlant(
                    plant = current,
                    onRadiusMinus = { scope.launch { vm.upsertPlant(current.copy(radius = (current.radius - 5f).coerceAtLeast(10f))) } },
                    onRadiusPlus = { scope.launch { vm.upsertPlant(current.copy(radius = (current.radius + 5f).coerceAtMost(300f))) } },
                    onDelete = { scope.launch { vm.deletePlant(current) }; selectedPlant = null },
                    onEdit = { showEditor = true }
                )
            }

            if (showEditor && current != null) {
                ModalBottomSheet(onDismissRequest = {
                    showEditor = false
                    if (isCreating) { selectedPlant = null }
                    isCreating = false
                }, sheetState = bottomSheetState) {
                    PlantEditor(
                        plant = current,
                        vm = vm,
                        onSave = { updated ->
                            scope.launch { vm.upsertPlant(updated) }
                            showEditor = false
                            isCreating = false
                            selectedPlant = updated
                        },
                        onCancel = {
                            showEditor = false
                            if (isCreating) { selectedPlant = null }
                            isCreating = false
                        },
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
