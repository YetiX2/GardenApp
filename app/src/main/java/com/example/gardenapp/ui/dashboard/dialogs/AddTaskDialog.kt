package com.example.gardenapp.ui.dashboard.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// TODO: Move to a shared file
private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (plant: PlantEntity, type: TaskType, due: LocalDateTime, notes: String?) -> Unit, // MODIFIED
    plants: List<PlantEntity>
) {
    var selectedPlant by remember { mutableStateOf(plants.firstOrNull()) }
    var plantMenuExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") } // ADDED
    var selectedType by remember { mutableStateOf(TaskType.WATER) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { 
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = LocalDate.ofEpochDay(it / (1000 * 60 * 60 * 24))
                    }
                    showDatePicker = false 
                }) {
                    Text("OK")
                }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = plantMenuExpanded, onExpandedChange = { plantMenuExpanded = !plantMenuExpanded }) {
                    OutlinedTextField(
                        value = selectedPlant?.title ?: "Выберите растение",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Растение") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = plantMenuExpanded, onDismissRequest = { plantMenuExpanded = false }) {
                        plants.forEach { plant ->
                            DropdownMenuItem(
                                text = { Text(plant.title) },
                                onClick = { selectedPlant = plant; plantMenuExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }) {
                    OutlinedTextField(
                        value = selectedType.toRussian(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип задачи") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        TaskType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.toRussian()) },
                                onClick = { selectedType = type; typeMenuExpanded = false }
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = selectedDate.toString(), onValueChange = {}, readOnly = true, label = { Text("Дата") }, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Заметка (необязательно)") }) // ADDED

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlant?.let { onAddTask(it, selectedType, selectedDate.atStartOfDay(), notes) }
                },
                enabled = selectedPlant != null
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
