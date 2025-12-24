package com.example.gardenapp.ui.plant.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.CareRuleEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.icon

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
fun AddCareRuleDialog(
    initialRule: CareRuleEntity? = null,
    onDismiss: () -> Unit,
    onAddRule: (TaskType, Int, String?) -> Unit // UPDATED signature
) {
    var selectedType by remember { mutableStateOf(initialRule?.type ?: TaskType.WATER) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var days by remember { mutableStateOf(initialRule?.everyDays?.toString() ?: "3") }
    var note by remember { mutableStateOf(initialRule?.note ?: "") } // ADDED

    val title = if (initialRule == null) "Новое правило ухода" else "Редактировать правило"

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
                        leadingIcon = { Icon(selectedType.icon, contentDescription = null) }, // ADDED
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                        TaskType.values().forEach { taskType ->
                            DropdownMenuItem(
                                text = { Text(taskType.toRussian()) },
                                leadingIcon = { Icon(taskType.icon, contentDescription = null) }, // ADDED
                                onClick = { selectedType = taskType; typeMenuExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(value = days, onValueChange = { days = it.filter(Char::isDigit) }, label = { Text("Повторять каждые (дней)") })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка (опционально)") }) // ADDED
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysInt = days.toIntOrNull()
                    if (daysInt != null) {
                        onAddRule(selectedType, daysInt, note.ifBlank { null }) // UPDATED
                    }
                },
                enabled = days.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
