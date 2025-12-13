package com.example.gardenapp.ui.plan

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.ui.plan.PlantEditor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenPlanScreen(
    gardenId: String,
    onBack: () -> Unit,
    onOpenPlant: (String) -> Unit,
    onOpenGarden: (String) -> Unit,
    vm: PlanVm = hiltViewModel()
) {
    LaunchedEffect(gardenId) { vm.loadGarden(gardenId) }

    val scope = rememberCoroutineScope()
    val garden by vm.currentGarden
    var plants by remember { mutableStateOf(emptyList<PlantEntity>()) }
    var childGardens by remember { mutableStateOf<List<GardenEntity>>(emptyList()) }
    LaunchedEffect(gardenId) { vm.plantsFlow(gardenId).collectLatest { plants = it } }
    LaunchedEffect(gardenId) { vm.childGardensFlow(gardenId).collectLatest { childGardens = it } }

    val state = rememberGardenPlanState(garden = garden, coroutineScope = scope)
    var showEditor by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // When a plant is selected by dragging, open the editor
    // This is a side-effect that connects the state to the UI action
    LaunchedEffect(state.selectedPlant) {
        if (state.selectedPlant != null && !isCreating) {
            // Decide if you want to open the bottom sheet directly
            // or navigate to a new screen via onOpenPlant
            // For now, let's just log it.
            // onOpenPlant(state.selectedPlant!!.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(garden?.name ?: "План сада") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад") } },
                actions = {
                    IconButton({ state.snapToGrid = !state.snapToGrid }) { Icon(imageVector = if (state.snapToGrid) Icons.Outlined.GridOn else Icons.Outlined.GridOff, contentDescription = "Привязка к сетке") }
                    IconButton({ state.resetView() }) { Icon(imageVector = Icons.Outlined.CenterFocusStrong, contentDescription = "Сбросить вид") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                val centerWorld = state.screenToWorld(Offset(state.canvasSize.width / 2f, state.canvasSize.height / 2f))
                val newPlant = PlantEntity(UUID.randomUUID().toString(), gardenId, "Новое растение", null, null, centerWorld.x, centerWorld.y, 35f, LocalDate.now())
                state.selectedPlant = newPlant
                isCreating = true
                showEditor = true
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            GardenCanvas(
                state = state,
                plants = plants,
                childGardens = childGardens,
                onPlantSelect = { state.selectedPlant = it },
                onPlantUpdate = { scope.launch { vm.upsertPlant(it) } },
                onGardenOpen = { onOpenGarden(it.id) },
                modifier = Modifier.fillMaxSize()
            )

            state.selectedPlant?.let { currentPlant ->
                if (!isCreating) {
                    ActionBarForPlant(
                        plant = currentPlant,
                        onRadiusMinus = { scope.launch { vm.upsertPlant(currentPlant.copy(radius = (currentPlant.radius - 5f).coerceAtLeast(10f))) } },
                        onRadiusPlus = { scope.launch { vm.upsertPlant(currentPlant.copy(radius = (currentPlant.radius + 5f).coerceAtMost(300f))) } },
                        onDelete = { 
                            scope.launch { vm.deletePlant(currentPlant) } 
                            state.selectedPlant = null
                        },
                        onEdit = {
                            onOpenPlant(currentPlant.id)
                        }
                    )
                }
            }

            if (showEditor && state.selectedPlant != null) {
                ModalBottomSheet(onDismissRequest = {
                    showEditor = false
                    if (isCreating) { state.selectedPlant = null }
                    isCreating = false
                }, sheetState = bottomSheetState) {
                    PlantEditor(
                        plant = state.selectedPlant!!,
                        vm = vm,
                        onSave = { updated ->
                            scope.launch { vm.upsertPlant(updated) }
                            showEditor = false
                            isCreating = false
                            state.selectedPlant = updated // Keep it selected after saving
                        },
                        onCancel = {
                            showEditor = false
                            if (isCreating) { state.selectedPlant = null }
                            isCreating = false
                        },
                        onAddFertilizer = { date, grams, note -> scope.launch { vm.addFertilizer(state.selectedPlant!!.id, date, grams, note) } },
                        onDeleteFertilizer = { item -> scope.launch { vm.deleteFertilizer(item) } },
                        onAddHarvest = { date, kg, note -> scope.launch { vm.addHarvest(state.selectedPlant!!.id, date, kg, note) } },
                        onDeleteHarvest = { item -> scope.launch { vm.deleteHarvest(item) } },
                        onAddCareRule = { type, start, everyDays, everyMonths -> scope.launch { vm.addCareRule(state.selectedPlant!!.id, type, start, everyDays, everyMonths) } },
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
            FilledTonalIconButton(onClick = onEdit) { Icon(imageVector = Icons.Default.Edit, contentDescription = "Редактировать") }
            FilledTonalIconButton(onClick = onRadiusMinus) { Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = "Уменьшить радиус") }
            FilledTonalIconButton(onClick = onRadiusPlus) { Icon(imageVector = Icons.Default.Add, contentDescription = "Увеличить радиус") }
            FilledTonalIconButton(onClick = onDelete) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить") }
        }
    }
}
