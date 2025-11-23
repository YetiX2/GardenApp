package com.example.gardenapp.ui.gardenlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GardenListVm @Inject constructor(private val repo: GardenRepository): androidx.lifecycle.ViewModel() {
    val gardens = repo.gardens()
    suspend fun add(name: String, w: Int, h: Int, step: Int) = repo.upsertGarden(name, w, h, step)
    suspend fun delete(g: GardenEntity) = repo.deleteGarden(g)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenListScreen(onOpen: (String) -> Unit, vm: GardenListVm = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val gardens by vm.gardens.collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<GardenEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои сады") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editTarget = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(gardens, key = { it.id }) { g ->
                ElevatedCard(onClick = { onOpen(g.id) }) {
                    ListItem(
                        headlineContent = { Text(g.name) },
                        supportingContent = { Text("${g.widthCm}×${g.heightCm} см • шаг ${g.gridStepCm} см") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { editTarget = g; showDialog = true }) { Icon(Icons.Default.Edit, contentDescription = null) }
                                IconButton(onClick = { scope.launch { vm.delete(g) } }) { Icon(Icons.Default.Delete, contentDescription = null) }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        GardenDialog(
            initial = editTarget,
            onDismiss = { showDialog = false },
            onSave = { name, w, h, step ->
                scope.launch {
                    if (editTarget == null) {
                        vm.add(name, w, h, step)
                    } else {
                        // Простой путь: удалить и создать новый (ID изменится)
                        vm.delete(editTarget!!)
                        vm.add(name, w, h, step)
                    }
                    showDialog = false
                }
            }
        )
    }
}

@Composable
private fun GardenDialog(
    initial: GardenEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "Мой сад") }
    var w by remember { mutableStateOf((initial?.widthCm ?: 1000).toString()) }
    var h by remember { mutableStateOf((initial?.heightCm ?: 600).toString()) }
    var step by remember { mutableStateOf((initial?.gridStepCm ?: 50).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новый сад" else "Редактировать сад") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Название") })
                OutlinedTextField(w, { w = it.filter { c -> c.isDigit() } }, label = { Text("Ширина (см)") })
                OutlinedTextField(h, { h = it.filter { c -> c.isDigit() } }, label = { Text("Высота (см)") })
                OutlinedTextField(step, { step = it.filter { c -> c.isDigit() } }, label = { Text("Шаг сетки (см)") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(name, w.toIntOrNull() ?: 1000, h.toIntOrNull() ?: 600, step.toIntOrNull() ?: 50)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
