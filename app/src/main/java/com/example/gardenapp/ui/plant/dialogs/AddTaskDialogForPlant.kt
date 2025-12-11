package com.example.gardenapp.ui.plant.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskType
import java.time.LocalDateTime

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
fun AddTaskDialogForPlant(
    onDismiss: () -> Unit,
    onAddTask: (type: TaskType, due: LocalDateTime, notes: String?) -> Unit // MODIFIED
) {
    var selectedType by remember { mutableStateOf(TaskType.WATER) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") } // ADDED
    // TODO: Add a date/time picker instead of hardcoding
    val dueDate = LocalDateTime.now().plusDays(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Task Type selection dropdown
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметка (необязательно)") },
                    minLines = 3 // ADDED THIS
                )

                Text("Срок выполнения: ${dueDate.toLocalDate()}")
            }
        },
        confirmButton = {
            Button(onClick = { onAddTask(selectedType, dueDate, notes.ifBlank { null }) }) { Text("Добавить") } // MODIFIED
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
