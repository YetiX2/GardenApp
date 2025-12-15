package com.example.gardenapp.ui.gardenlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenListScreen(onOpen: (String) -> Unit, onBack: () -> Unit, vm: GardenListVm = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val gardens by vm.gardens.collectAsState(initial = emptyList())

    var showEditDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<GardenEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<GardenEntity?>(null) }
    var expandedMenuGardenId by remember { mutableStateOf<String?>(null) } // ADDED

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Мои грядки / участок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editTarget = null; showEditDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(gardens, key = { it.id }) { g ->
                ElevatedCard(onClick = { onOpen(g.id) }) {
                    ListItem(
                        headlineContent = { Text(g.name) },
                        supportingContent = {
                            val typeText = g.type.toRussian()
                            Text("$typeText • ${g.widthCm}×${g.heightCm} см")
                        },
                        trailingContent = {
                            // REPLACED Row with Box and DropdownMenu
                            Box {
                                IconButton(onClick = { expandedMenuGardenId = g.id }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Дополнительно")
                                }
                                DropdownMenu(
                                    expanded = expandedMenuGardenId == g.id,
                                    onDismissRequest = { expandedMenuGardenId = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Редактировать") },
                                        onClick = { 
                                            editTarget = g
                                            showEditDialog = true
                                            expandedMenuGardenId = null 
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Удалить") },
                                        onClick = { 
                                            showDeleteConfirm = g
                                            expandedMenuGardenId = null 
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        GardenEditDialog(
            initial = editTarget,
            allGardens = gardens,
            onDismiss = { showEditDialog = false },
            onSave = { name, w, h, step, zone, type, parentId ->
                scope.launch {
                    val newGardenId = vm.upsert(editTarget?.id, name, w, h, step, zone, type, parentId)
                    showEditDialog = false
                    if (editTarget == null) { onOpen(newGardenId) }
                }
            }
        )
    }

    showDeleteConfirm?.let { gardenToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Подтвердите удаление") },
            text = { Text("Вы уверены, что хотите удалить сад \"${gardenToDelete.name}\"? Все связанные с ним растения и данные будут также удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { vm.delete(gardenToDelete) }
                        showDeleteConfirm = null
                    }
                ) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("Отмена") } }
        )
    }
}

private fun GardenType.toRussian(): String = when (this) {
    GardenType.PLOT -> "Участок"
    GardenType.GREENHOUSE -> "Теплица"
    GardenType.BED -> "Грядка"
    GardenType.BUILDING -> "Строение"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GardenEditDialog(
    initial: GardenEntity?,
    allGardens: List<GardenEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int, Int?, GardenType, String?) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "Мой сад") }
    var w by remember { mutableStateOf((initial?.widthCm ?: 1000).toString()) }
    var h by remember { mutableStateOf((initial?.heightCm ?: 600).toString()) }
    var step by remember { mutableStateOf((initial?.gridStepCm ?: 50).toString()) }
    var climateZone by remember { mutableStateOf(initial?.climateZone?.toString() ?: "") } // ADDED
    var type by remember { mutableStateOf(initial?.type ?: GardenType.PLOT) }
    var parentId by remember { mutableStateOf(initial?.parentId) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var parentMenuExpanded by remember { mutableStateOf(false) }

    // DYNAMICALLY FILTER POSSIBLE PARENTS
    val possibleParents = remember(type, allGardens) {
        when (type) {
            GardenType.GREENHOUSE, GardenType.BUILDING -> // MODIFIED
                allGardens.filter { it.type == GardenType.PLOT && it.id != initial?.id }
            GardenType.BED ->
                allGardens.filter { (it.type == GardenType.PLOT || it.type == GardenType.GREENHOUSE) && it.id != initial?.id }
            else -> emptyList()
        }
    }

    // Auto-clear parent if it becomes invalid
    LaunchedEffect(possibleParents) {
        if (parentId != null && possibleParents.none { it.id == parentId }) {
            parentId = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый участок" else "Редактировать участок") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Название") })

                ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }) {
                    OutlinedTextField(type.toRussian(), {}, readOnly = true, label = { Text("Тип") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) })
                    ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        GardenType.values().forEach {
                            DropdownMenuItem(text = { Text(it.toRussian()) }, onClick = {
                                type = it
                                typeMenuExpanded = false
                                if (it == GardenType.PLOT) parentId = null // Reset parent if type is PLOT
                            })
                        }
                    }
                }

                if (type != GardenType.PLOT) {
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = parentMenuExpanded, onExpandedChange = { parentMenuExpanded = !parentMenuExpanded }) {
                        val selectedParentName = possibleParents.find { it.id == parentId }?.name ?: ""
                        OutlinedTextField(value = selectedParentName, onValueChange = {}, readOnly = true, label = { Text("Родительский участок") }, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentMenuExpanded) })
                        ExposedDropdownMenu(expanded = parentMenuExpanded, onDismissRequest = { parentMenuExpanded = false }) {
                            if (possibleParents.isEmpty()) {
                                DropdownMenuItem(text = { Text("Нет доступных участков") }, onClick = {}, enabled = false)
                            } else {
                                possibleParents.forEach { plot ->
                                    DropdownMenuItem(text = { Text(plot.name) }, onClick = {
                                        parentId = plot.id
                                        parentMenuExpanded = false
                                    })
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(w, { w = it.filter(Char::isDigit) }, label = { Text("Ширина (см)") })
                OutlinedTextField(h, { h = it.filter(Char::isDigit) }, label = { Text("Высота (см)") })
                OutlinedTextField(step, { step = it.filter(Char::isDigit) }, label = { Text("Шаг сетки (см)") })
                OutlinedTextField(climateZone, { climateZone = it.filter(Char::isDigit) }, label = { Text("Зона морозостойкости (USDA)") }) // ADDED
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(name, w.toIntOrNull() ?: 1000, h.toIntOrNull() ?: 600, step.toIntOrNull() ?: 50, climateZone.toIntOrNull()?:4, type, parentId)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}