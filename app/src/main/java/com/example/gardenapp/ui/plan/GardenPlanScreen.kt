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
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LabelOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.GridOff
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.ui.plan.PlantEditor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import com.example.gardenapp.ui.DefaultColors


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
    LaunchedEffect(gardenId) { vm.plantsFlow(gardenId).collectLatest { plants = it } }
    LaunchedEffect(gardenId) { vm.childGardensFlow(gardenId).collectLatest { childGardens = it } }

    val plantColor by vm.plantColor.collectAsState(initial = DefaultColors.plantColor)
    val bedColor by vm.bedColor.collectAsState(initial = DefaultColors.bedColor)
    val greenhouseColor by vm.greenhouseColor.collectAsState(initial = DefaultColors.greenhouseColor)
    val buildingColor by vm.buildingColor.collectAsState(initial = DefaultColors.buildingColor)
    val gridColor by vm.gridColor.collectAsState(initial = DefaultColors.gridColor())
    val gardenBackgroundColor by vm.gardenBackgroundColor.collectAsState(initial = DefaultColors.backgroundColor)
    val textColor by vm.textColor.collectAsState(initial = DefaultColors.textColor())
    val selectedStroke by vm.selectedStrokeColor.collectAsState(initial = DefaultColors.selectedStrokeColor())

    val state = rememberGardenPlanState(garden = garden, coroutineScope = scope)
    
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(garden?.name ?: "План сада") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } },
                actions = {
                    IconButton(onClick = { state.showNames = !state.showNames }) { Icon(imageVector = if (state.showNames) Icons.Default.TextIncrease else Icons.Default.TextDecrease, contentDescription = "Показать/скрыть названия") }
                    IconButton(onClick = { state.isLocked = !state.isLocked }) { Icon(imageVector = if (state.isLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Заблокировать перемещение") }
                    IconButton(onClick = { showPlantList = true }) { Icon(imageVector = Icons.Default.Grass, contentDescription = "Список растений") }
                    IconButton({ state.snapToGrid = !state.snapToGrid }) { Icon(imageVector = if (state.snapToGrid) Icons.Outlined.GridOn else Icons.Outlined.GridOff, contentDescription = "Привязка к сетке") }
                    //IconButton({ state.resetView() }) { Icon(imageVector = Icons.Outlined.CenterFocusStrong, contentDescription = "Сбросить вид") }
                    // REMOVED Settings button from here
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                val centerWorld = state.screenToWorld(Offset(state.canvasSize.width / 2f, state.canvasSize.height / 2f))
                val newPlant = PlantEntity(UUID.randomUUID().toString(), gardenId, "", null, null, centerWorld.x, centerWorld.y, 35f, LocalDate.now())
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
                plantColor = Color(plantColor),
                bedColor = Color(bedColor),
                greenhouseColor = Color(greenhouseColor),
                buildingColor = Color(buildingColor),
                gridColor = Color(gridColor),
                gardenBackgroundColor = Color(gardenBackgroundColor),
                textColor = Color(textColor),
                selectedStrokeColor = Color(selectedStroke),
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
                modifier = Modifier.fillMaxSize()
            )

            state.selectedPlant?.let {
                if (!isCreating) {
                    ActionBarForPlant(
                        plant = it,
                        onRadiusMinus = { scope.launch { vm.upsertPlant(it.copy(radius = (it.radius - 5f).coerceAtLeast(10f))) } },
                        onRadiusPlus = { scope.launch { vm.upsertPlant(it.copy(radius = (it.radius + 5f).coerceAtMost(300f))) } },
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
                        onPlantClick = { plant ->
                            showPlantList = false
                            onOpenPlant(plant.id)
                        }
                    )
                }
            }

            if (showEditor && state.selectedPlant != null) {
                ModalBottomSheet(onDismissRequest = {
                    showEditor = false
                    if (isCreating) { state.selectedPlant = null }
                    isCreating = false
                }, sheetState = editorSheetState) {
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
