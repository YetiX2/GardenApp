package com.example.gardenapp.ui.plan

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.outlined.GridOff
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.ReferenceCultureEntity
import com.example.gardenapp.data.db.ReferenceVarietyEntity
import com.example.gardenapp.data.db.TaskInstanceEntity
import com.example.gardenapp.ui.theme.LocalIsDarkTheme
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
    var childGardens by remember { mutableStateOf(emptyList<GardenEntity>()) }
    var pendingTasks by remember { mutableStateOf(emptyList<TaskInstanceEntity>()) }
    var varieties by remember { mutableStateOf(emptyList<ReferenceVarietyEntity>()) }
    var cultures by remember { mutableStateOf(emptyList<ReferenceCultureEntity>()) }

    LaunchedEffect(gardenId) { vm.plantsFlow(gardenId).collectLatest { plants = it } }
    LaunchedEffect(gardenId) { vm.childGardensFlow(gardenId).collectLatest { childGardens = it } }
    LaunchedEffect(gardenId) { vm.getPendingTasksForGardens(gardenId).collectLatest { pendingTasks = it } }
    LaunchedEffect(Unit) { vm.getAllVarieties().collectLatest { varieties = it } }
    LaunchedEffect(Unit) { vm.getAllCultures().collectLatest { cultures = it } }

    val state = rememberGardenPlanState(garden = garden, coroutineScope = scope)

    // сохраняем selection при обновлении списков
    LaunchedEffect(plants) {
        state.selectedPlant?.let { currentSelected ->
            state.selectedPlant = plants.find { p -> p.id == currentSelected.id }
        }
    }
    LaunchedEffect(childGardens) {
        state.selectedChildGarden?.let { currentSelected ->
            state.selectedChildGarden = childGardens.find { g -> g.id == currentSelected.id }
        }
    }

    var showEditor by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var showPlantList by remember { mutableStateOf(false) }
    val editorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val plantListSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val darkTheme = LocalIsDarkTheme.current // можно заменить на свой флаг из SettingsViewModel, если есть

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(garden?.name ?: "План сада") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { state.showNames = !state.showNames }) {
                        Icon(
                            imageVector = if (state.showNames) Icons.Default.TextIncrease else Icons.Default.TextDecrease,
                            contentDescription = "Показать/скрыть названия"
                        )
                    }
                    IconButton(onClick = { state.isLocked = !state.isLocked }) {
                        Icon(
                            imageVector = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Заблокировать перемещение"
                        )
                    }
                    IconButton(onClick = { showPlantList = true }) {
                        Icon(
                            imageVector = Icons.Default.Grass,
                            contentDescription = "Список растений"
                        )
                    }
                    IconButton(onClick = { state.snapToGrid = !state.snapToGrid }) {
                        Icon(
                            imageVector = if (state.snapToGrid) Icons.Outlined.GridOn else Icons.Outlined.GridOff,
                            contentDescription = "Привязка к сетке"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val centerWorld = state.screenToWorld(
                        Offset(
                            state.canvasSize.width / 2f,
                            state.canvasSize.height / 2f
                        )
                    )
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
                    state.selectedPlant = newPlant
                    isCreating = true
                    showEditor = true
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            // 👉 Подключаем тему редактора, цвета берём из настроек
            GardenEditorThemeFromSettings(darkTheme = darkTheme) {
                val colors = GardenEditorTheme.colors

                GardenCanvas(
                    state = state,
                    plants = plants,
                    childGardens = childGardens,
                    pendingTasks = pendingTasks, // ADDED
                    varieties = varieties,
                    cultures = cultures,
                    plantColor = colors.plant,
                    bedColor = colors.bed,
                    greenhouseColor = colors.greenhouse,
                    buildingColor = colors.building,
                    gridColor = colors.grid,
                    gardenBackgroundColor = colors.background,
                    textColor = colors.text,
                    selectedStrokeColor = colors.selectedStroke,
                    onPlantSelect = { state.selectedPlant = it },
                    onGardenSelect = { state.selectedChildGarden = it },
                    onPlantDrag = { updatedPlant ->
                        state.selectedPlant = updatedPlant
                        plants = plants.map { if (it.id == updatedPlant.id) updatedPlant else it }
                    },
                    onGardenDrag = { updatedGarden ->
                        state.selectedChildGarden = updatedGarden
                        childGardens = childGardens.map { if (it.id == updatedGarden.id) updatedGarden else it }
                    },
                    onPlantUpdate = { scope.launch { vm.upsertPlant(it) } },
                    onGardenUpdate = { scope.launch { vm.upsertGarden(it) } },
                    onGardenOpen = { onOpenGarden(it.id) },
                    onPlantOpen = { onOpenPlant(it.id) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            state.selectedPlant?.let {
                if (!isCreating) {
                    ActionBarForPlant(
                        plant = it,
                        onRadiusMinus = {
                            scope.launch {
                                vm.upsertPlant(
                                    it.copy(
                                        radius = (it.radius - 5f).coerceAtLeast(10f)
                                    )
                                )
                            }
                        },
                        onRadiusPlus = {
                            scope.launch {
                                vm.upsertPlant(
                                    it.copy(
                                        radius = (it.radius + 5f).coerceAtMost(300f)
                                    )
                                )
                            }
                        },
                        onDelete = {
                            scope.launch { vm.deletePlant(it) }
                            state.selectedPlant = null
                        },
                        onEdit = { onOpenPlant(it.id) }
                    )
                }
            }

            if (showPlantList) {
                ModalBottomSheet(
                    onDismissRequest = { showPlantList = false },
                    sheetState = plantListSheetState
                ) {
                    PlantListSheet(
                        plants = plants,
                        varieties = varieties,
                        cultures = cultures,
                        onPlantClick = { plant ->
                            showPlantList = false
                            onOpenPlant(plant.id)
                        }
                    )
                }
            }

            if (showEditor && state.selectedPlant != null) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showEditor = false
                        if (isCreating) { state.selectedPlant = null }
                        isCreating = false
                    },
                    sheetState = editorSheetState
                ) {
                    PlantEditor(
                        plant = state.selectedPlant!!,
                        vm = vm,
                        onSave = { updated ->
                            scope.launch { vm.upsertPlant(updated) }
                            showEditor = false
                            isCreating = false
                            state.selectedPlant = updated
                        },
                        onCancel = {
                            showEditor = false
                            if (isCreating) { state.selectedPlant = null }
                            isCreating = false
                        }
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
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                plant.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            FilledTonalIconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Редактировать")
            }
            FilledTonalIconButton(onClick = onRadiusMinus) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle,
                    contentDescription = "Уменьшить радиус"
                )
            }
            FilledTonalIconButton(onClick = onRadiusPlus) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Увеличить радиус"
                )
            }
            FilledTonalIconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить"
                )
            }
        }
    }
}
