package com.example.gardenapp.ui.plant.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.icon
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.HARVEST -> "Сбор урожая"
    TaskType.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialogForPlant(
    onDismiss: () -> Unit,
    onAddTask: (TaskType, LocalDateTime, String?, Float?, String?) -> Unit,
) {
    var selectedType by remember { mutableStateOf(TaskType.WATER) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("г") }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val showAmount = selectedType in listOf(TaskType.FERTILIZE, TaskType.WATER, TaskType.TREAT)

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
                ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = !typeMenuExpanded }) {
                    OutlinedTextField(
                        value = selectedType.toRussian(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип задачи") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        leadingIcon = { Icon(selectedType.icon, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        TaskType.values().forEach { taskType ->
                            DropdownMenuItem(
                                text = { Text(taskType.toRussian()) },
                                leadingIcon = { Icon(taskType.icon, contentDescription = null) },
                                onClick = { selectedType = taskType; typeMenuExpanded = false }
                            )
                        }
                    }
                }
                if (showAmount) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Количество") },
                            modifier = Modifier.weight(1f)
                        )
                        ExposedDropdownMenuBox(
                            expanded = unitMenuExpanded,
                            onExpandedChange = { unitMenuExpanded = !unitMenuExpanded },
                            modifier = Modifier.weight(0.5f)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitMenuExpanded) }
                            )
                            ExposedDropdownMenu(expanded = unitMenuExpanded, onDismissRequest = { unitMenuExpanded = false }) {
                                listOf("г", "мл", "л").forEach { u ->
                                    DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitMenuExpanded = false })
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка (опционально)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddTask(selectedType, selectedDate.atStartOfDay(), note.ifBlank { null }, amount.toFloatOrNull(), if(showAmount) unit else null)
                }
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
