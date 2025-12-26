package com.example.gardenapp.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.db.ReferenceCultureEntity
import com.example.gardenapp.ui.theme.GardenAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonStatsScreen(
    vm: ISeasonStatsVm = hiltViewModel<SeasonStatsVm>(),
    onNavigateBack: () -> Unit = {},
    onOpenGarden: (String) -> Unit = {},
    onOpenPlant: (String) -> Unit = {}
) {
    val summary by vm.seasonSummary.collectAsState()
    val statsByCulture by vm.statsByCulture.collectAsState()
    val statsByGarden by vm.statsByGarden.collectAsState()

    var selectedFilter by remember { mutableStateOf("Урожай") }

    val currentYear = LocalDate.now().year

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика сезона $currentYear") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Filter Chips ---
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selectedFilter == "Урожай", onClick = { selectedFilter = "Урожай" }, label = { Text("Урожай") })
                    FilterChip(selected = selectedFilter == "Удобрения", onClick = { selectedFilter = "Удобрения" }, label = { Text("Удобрения") })
                    FilterChip(selected = selectedFilter == "Задачи", onClick = { selectedFilter = "Задачи" }, label = { Text("Задачи") })
                }
            }

            // --- Totals ---
            item {
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Итоги сезона", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Всего собрано: ${String.format(Locale.getDefault(), "%.1f", summary.totalHarvest)} кг")
                        Text("Всего внесено удобрений: ${summary.totalTreatments} раз")
                        // Text("Всего обработок от вредителей: X") // Placeholder
                    }
                }
            }

            // --- By Culture ---
            item {
                Text("По культурам", style = MaterialTheme.typography.titleMedium)
            }
            items(statsByCulture) {
                ListItem(
                    modifier = Modifier.clickable { it.representativePlantId?.let(onOpenPlant) },
                    headlineContent = {
                        Text(it.culture.title, fontWeight = FontWeight.Bold)
                    },
                    supportingContent = {
                        Text("${String.format(Locale.getDefault(), "%.1f", it.totalHarvest)} кг • ${it.totalFertilizer} подкормок")
                    }
                )
            }

            // --- By Garden ---
            item {
                Text("По участкам", style = MaterialTheme.typography.titleMedium)
            }
            items(statsByGarden) {
                ListItem(
                    modifier = Modifier.clickable { onOpenGarden(it.garden.id) },
                    headlineContent = { Text(it.garden.name) },
                    supportingContent = { Text("${String.format(Locale.getDefault(), "%.1f", it.totalHarvest)} кг") }
                )
            }
        }
    }
}

class PreviewSeasonStatsVm : ISeasonStatsVm {
    override val seasonSummary: StateFlow<SeasonSummary> = MutableStateFlow(
        SeasonSummary(activePlants = 12, totalHarvest = 18.4f, totalTreatments = 5)
    )
    override val statsByCulture: StateFlow<List<CultureStats>> = MutableStateFlow(listOf(
        CultureStats(culture = ReferenceCultureEntity("tomato", "", "Томаты"), representativePlantId = "p1", totalHarvest = 9.2f, totalFertilizer = 3),
        CultureStats(culture = ReferenceCultureEntity("strawberry", "", "Клубника"), representativePlantId = "p2", totalHarvest = 4.5f, totalFertilizer = 2),
        CultureStats(culture = ReferenceCultureEntity("currant_black", "", "Смородина"), representativePlantId = "p3", totalHarvest = 2.1f, totalTreatments = 1),
    ))
    override val statsByGarden: StateFlow<List<GardenStats>> = MutableStateFlow(listOf(
        GardenStats(garden = GardenEntity("g1", "Теплица 1", 0, 0, 0, GardenType.GREENHOUSE), totalHarvest = 11.0f),
        GardenStats(garden = GardenEntity("g2", "Грядка №3", 0, 0, 0, GardenType.BED), totalHarvest = 3.4f),
        GardenStats(garden = GardenEntity("g3", "Ягодник", 0, 0, 0, GardenType.PLOT), totalHarvest = 4.0f),
    ))
}

@Preview(showBackground = true)
@Composable
fun SeasonStatsScreenPreview() {
    GardenAppTheme {
        SeasonStatsScreen(vm = PreviewSeasonStatsVm())
    }
}
