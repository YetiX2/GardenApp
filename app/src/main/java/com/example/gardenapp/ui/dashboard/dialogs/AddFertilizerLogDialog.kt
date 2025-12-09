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
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFertilizerLogDialog(
    onDismiss: () -> Unit,
    onAddLog: (plant: PlantEntity, grams: Float, date: LocalDate, note: String?) -> Unit,
    plants: List<PlantEntity>
) {
    var selectedPlant by remember { mutableStateOf<PlantEntity?>(null) }
    var plantMenuExpanded by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

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
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Отмена") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись об удобрении") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = plantMenuExpanded, onExpandedChange = { plantMenuExpanded = !plantMenuExpanded }) {
                    OutlinedTextField(
                        value = selectedPlant?.title ?: "",
                        onValueChange = {}, 
                        readOnly = true,
                        label = { Text("Растение") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantMenuExpanded) }
                    )
                    ExposedDropdownMenu(expanded = plantMenuExpanded, onDismissRequest = { plantMenuExpanded = false }) {
                        plants.forEach {
                            DropdownMenuItem(text = { Text(it.title) }, onClick = { selectedPlant = it; plantMenuExpanded = false })
                        }
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("Количество (г)") })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Заметка (необязательно)") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = selectedDate.toString(), onValueChange = {}, readOnly = true, label = { Text("Дата") }, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountFloat = amount.toFloatOrNull()
                    if (selectedPlant != null && amountFloat != null) {
                        onAddLog(selectedPlant!!, amountFloat, selectedDate, note.ifBlank { null })
                    }
                },
                enabled = selectedPlant != null && amount.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
