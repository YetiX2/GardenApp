package com.example.gardenapp.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.*
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class) // ADDED ExperimentalLayoutApi
@Composable
fun PlantEditor(
    plant: PlantEntity,
    vm: PlanVm,
    onSave: (PlantEntity) -> Unit,
    onCancel: () -> Unit
) {
    val garden by vm.currentGarden
    val gardenZone = garden?.climateZone

    var title by remember { mutableStateOf(plant.title) }
    var plantedAt by remember { mutableStateOf(plant.plantedAt) }

    val groups by vm.referenceGroups.collectAsState(initial = emptyList())
    val allCultures by vm.getAllCultures().collectAsState(initial = emptyList())
    val allVarieties by vm.getAllVarieties().collectAsState(initial = emptyList())

    var selectedGroup by remember { mutableStateOf<ReferenceGroupEntity?>(null) }
    val cultures by remember(selectedGroup) { selectedGroup?.let { vm.getCulturesByGroup(it.id) } ?: flowOf(emptyList()) }.collectAsState(initial = emptyList())
    var selectedCulture by remember { mutableStateOf<ReferenceCultureEntity?>(null) }
    val varieties by remember(selectedCulture) { selectedCulture?.let { vm.getVarietiesByCulture(it.id) } ?: flowOf(emptyList()) }.collectAsState(initial = emptyList())
    var selectedVariety by remember { mutableStateOf<ReferenceVarietyEntity?>(null) }
    val tags by remember(selectedVariety) { selectedVariety?.let { vm.getTagsForVariety(it.id) } ?: flowOf(emptyList()) }.collectAsState(initial = emptyList())

    LaunchedEffect(plant, allCultures, allVarieties, groups) {
        if (plant.varietyId != null && allVarieties.isNotEmpty() && allCultures.isNotEmpty() && groups.isNotEmpty()) {
            val variety = allVarieties.find { it.id == plant.varietyId }
            val culture = variety?.let { allCultures.find { c -> c.id == it.cultureId } }
            val group = culture?.let { groups.find { g -> g.id == it.groupId } }

            selectedGroup = group
            selectedCulture = culture
            selectedVariety = variety
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Добавляем растение", style = MaterialTheme.typography.titleLarge)
            ReferenceDropdown(label = "Группа", items = groups, selected = selectedGroup, onSelected = { selectedGroup = it; selectedCulture = null; selectedVariety = null }, itemTitle = { it.title })
            ReferenceDropdown(label = "Культура", items = cultures, selected = selectedCulture, onSelected = { selectedCulture = it; selectedVariety = null }, itemTitle = { it.title }, enabled = selectedGroup != null)
            
            var varietyMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = varietyMenuExpanded, onExpandedChange = { if (selectedCulture != null) varietyMenuExpanded = !varietyMenuExpanded }) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    value = selectedVariety?.title ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Сорт") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            selectedVariety?.hardiness?.let { Text("диапазон морозостойкости "+" (${it.min}-${it.max})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                            Spacer(Modifier.width(4.dp))
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = varietyMenuExpanded)
                        }
                    },
                    enabled = selectedCulture != null
                )

                ExposedDropdownMenu(expanded = varietyMenuExpanded, onDismissRequest = { varietyMenuExpanded = false }) {
                    varieties.forEach { variety ->
                        val recommendation = checkRecommendation(gardenZone, variety.hardiness)

                        DropdownMenuItem(
                            text = {
                                Text(
                                    variety.title,
                                    color = if (recommendation == RecommendationLevel.RECOMMENDED) LocalContentColor.current else MaterialTheme.colorScheme.outline
                                )
                            },
                            trailingIcon = {
                                when (recommendation) {
                                    RecommendationLevel.WARNING -> Icon(Icons.Outlined.Warning, "Незначительное несоответствие", tint = Color.Yellow)
                                    RecommendationLevel.NOT_RECOMMENDED -> Icon(Icons.Outlined.Error, "Не рекомендуется для вашей зоны", tint = MaterialTheme.colorScheme.error)
                                    else -> {}
                                }
                            },
                            onClick = { selectedVariety = variety; varietyMenuExpanded = false }
                        )
                    }
                }
            }


            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Собственное название (необязательно)") }, modifier = Modifier.fillMaxWidth())
            DateRow(label = "Дата посадки", date = plantedAt, onPick = { plantedAt = it })

            if (tags.isNotEmpty()) {
                // === MODIFIED BLOCK START ===
                Box(modifier = Modifier.heightIn(max = 150.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 8.dp).verticalScroll(rememberScrollState())
                    ) {
                        tags.forEach { tag -> AssistChip(onClick = { }, label = { Text("${tag.key}: ${tag.value}") }) }
                    }
                }
                // === MODIFIED BLOCK END ===
            }

            Row {
                Button(
                    onClick = { 
                        val finalTitle = title.ifBlank { selectedVariety?.title ?: selectedCulture?.title ?: "Растение" }
                        onSave(plant.copy(
                            title = finalTitle, 
                            variety = selectedVariety?.title,
                            varietyId = selectedVariety?.id,
                            plantedAt = plantedAt
                        ))
                    },
                    enabled = selectedCulture != null
                ) {
                    Text("Сохранить")
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onCancel) { Text("Отмена") }
            }
            Divider()
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ReferenceDropdown(
    label: String,
    items: List<T>,
    selected: T?,
    onSelected: (T) -> Unit,
    itemTitle: (T) -> String,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if(enabled) expanded = !expanded }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            value = selected?.let(itemTitle) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            enabled = enabled
        )
        if (enabled) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemTitle(item)) },
                        onClick = {
                            onSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRow(label: String, date: LocalDate, onPick: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = date.toString(), onValueChange = {}, enabled = false, label = { Text(label) })
        FilledTonalButton(onClick = { show = true }) { Icon(Icons.Default.DateRange, contentDescription = null); Spacer(Modifier.width(6.dp)); }
    }
    if (show) {
        DatePickerDialog(onDismissRequest = { show = false }, confirmButton = {}) {
            val state = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * 86_400_000)
            LaunchedEffect(state.selectedDateMillis) {
                val ms = state.selectedDateMillis ?: return@LaunchedEffect
                onPick(LocalDate.ofEpochDay(ms / 86_400_000))
            }
            DatePicker(state = state)
        }
    }
}


private fun checkRecommendation(gardenZone: Int?, varietyHardiness: HardinessEntity?): RecommendationLevel {
    if (gardenZone == null || varietyHardiness == null) {
        return RecommendationLevel.RECOMMENDED // No data to check against
    }

    val min = varietyHardiness.min
    val max = varietyHardiness.max

    return when {
        gardenZone in min..max -> RecommendationLevel.RECOMMENDED
        abs(gardenZone - min) <= 1 || abs(gardenZone - max) <= 1 -> RecommendationLevel.WARNING
        else -> RecommendationLevel.NOT_RECOMMENDED
    }
}
