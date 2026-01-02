package com.example.gardenapp.ui.plant.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.CareRuleEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.icon
import com.example.gardenapp.data.db.toRussian
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCareRuleDialog(
    initialRule: CareRuleEntity? = null,
    onDismiss: () -> Unit,
    onAddRule: (TaskType, Int, String?, Float?, String?, LocalDate?, LocalDate?) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialRule?.type ?: TaskType.WATER) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var days by remember { mutableStateOf(initialRule?.everyDays?.toString() ?: "3") }
    var note by remember { mutableStateOf(initialRule?.note ?: "") }
    var amount by remember { mutableStateOf(initialRule?.amount?.toString() ?: "") }
    var unit by remember { mutableStateOf(initialRule?.unit ?: "г") }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    // Date state
    var startDate by remember { mutableStateOf(initialRule?.startDate) }
    var endDate by remember { mutableStateOf(initialRule?.endDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd MMMM")

    val title = if (initialRule == null) "Новое правило ухода" else "Редактировать правило"
    val showAmount = selectedType in listOf(TaskType.FERTILIZE, TaskType.WATER, TaskType.TREAT)

    // Hoisted DatePicker state
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (startDate ?: LocalDate.now()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                OutlinedTextField(value = days, onValueChange = { days = it.filter(Char::isDigit) }, label = { Text("Повторять каждые (дней)") })

                Box {
                    OutlinedTextField(
                        value = startDate?.format(formatter) ?: "С даты посадки",
                        onValueChange = { },
                        label = { Text("Начало периода") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (startDate != null) {
                                IconButton(onClick = { startDate = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить дату")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Transparent,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showStartDatePicker = true })
                }


                Box {
                    OutlinedTextField(
                        value = endDate?.format(formatter) ?: "Бессрочно",
                        onValueChange = {},
                        label = { Text("Конец периода") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (endDate != null) {
                                IconButton(onClick = { endDate = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить дату")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.Transparent,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showEndDatePicker = true })
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
                    val daysInt = days.toIntOrNull()
                    if (daysInt != null) {
                        onAddRule(selectedType, daysInt, note.ifBlank { null }, amount.toFloatOrNull(), if(showAmount) unit else null, startDate, endDate)
                    }
                },
                enabled = days.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
