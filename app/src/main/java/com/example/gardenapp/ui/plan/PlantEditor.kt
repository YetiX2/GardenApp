package com.example.gardenapp.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantEditor(
    plant: PlantEntity,
    vm: PlanVm,
    onSave: (PlantEntity) -> Unit,
    onCancel: () -> Unit
) {
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var selectedCultureId by remember { mutableStateOf<String?>(null) }
    var selectedVarietyId by remember { mutableStateOf<String?>(null) }
    var plantName by remember { mutableStateOf(plant.title) }

    val groups by vm.referenceGroups.collectAsState(initial = emptyList())
    val cultures by remember(selectedGroupId) {
        selectedGroupId?.let { vm.getCulturesByGroup(it) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val varieties by remember(selectedCultureId) {
        selectedCultureId?.let { vm.getVarietiesByCulture(it) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())

    val lastUsedGroupId by vm.lastUsedGroupId.collectAsState()
    val lastUsedCultureId by vm.lastUsedCultureId.collectAsState()

    LaunchedEffect(plant, lastUsedGroupId, lastUsedCultureId) {
        if (plant.varietyId == null) { // only on create
            selectedGroupId = lastUsedGroupId
            selectedCultureId = lastUsedCultureId
        } else {
            // todo: pre-select on edit
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // --- Group Dropdown ---
        var expandedGroup by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expandedGroup, onExpandedChange = { expandedGroup = !expandedGroup }) {
            TextField(
                value = groups.find { it.id == selectedGroupId }?.title ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Группа") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGroup) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedGroup, onDismissRequest = { expandedGroup = false }) {
                groups.forEach {
                    DropdownMenuItem(
                        text = { Text(it.title) },
                        onClick = { selectedGroupId = it.id; selectedCultureId = null; selectedVarietyId = null; expandedGroup = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // --- Culture Dropdown ---
        var expandedCulture by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedCulture && selectedGroupId != null,
            onExpandedChange = { expandedCulture = !expandedCulture }
        ) {
            TextField(
                value = cultures.find { it.id == selectedCultureId }?.title ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Культура") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCulture) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedCulture, onDismissRequest = { expandedCulture = false }) {
                cultures.forEach {
                    DropdownMenuItem(
                        text = { Text(it.title) },
                        onClick = { selectedCultureId = it.id; selectedVarietyId = null; expandedCulture = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // --- Variety Dropdown ---
        var expandedVariety by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedVariety && selectedCultureId != null,
            onExpandedChange = { expandedVariety = !expandedVariety }
        ) {
            TextField(
                value = varieties.find { it.id == selectedVarietyId }?.title ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Сорт") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVariety) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expandedVariety, onDismissRequest = { expandedVariety = false }) {
                varieties.forEach {
                    DropdownMenuItem(
                        text = { Text(it.title) },
                        onClick = { selectedVarietyId = it.id; plantName = it.title; expandedVariety = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // --- Plant Name ---
        TextField(
            value = plantName,
            onValueChange = { plantName = it },
            label = { Text("Название растения (на плане)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onCancel) { Text("Отмена") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val variety = varieties.find { it.id == selectedVarietyId }
                if (selectedGroupId != null && selectedCultureId != null) {
                    vm.saveLastUsedIds(selectedGroupId!!, selectedCultureId!!)
                }
                onSave(
                    plant.copy(
                        title = plantName,
                        variety = variety?.title,
                        varietyId = selectedVarietyId
                    )
                )
            }) { Text("Сохранить") }
        }
    }
}
