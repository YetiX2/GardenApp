package com.example.gardenapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.ReferenceCultureEntity
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.ReferenceDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Month
import javax.inject.Inject

data class GardenStats(
    val garden: GardenEntity,
    val totalHarvest: Float = 0f
)

data class CultureStats(
    val culture: ReferenceCultureEntity,
    val totalHarvest: Float = 0f,
    val totalFertilizer: Int = 0,
    val totalTreatments: Int = 0 // Assuming treatments are a separate type of log later
)

interface ISeasonStatsVm {
    val seasonSummary: StateFlow<SeasonSummary>
    val statsByCulture: StateFlow<List<CultureStats>>
    val statsByGarden: StateFlow<List<GardenStats>>
}

@HiltViewModel
class SeasonStatsVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceRepo: ReferenceDataRepository
) : ViewModel(), ISeasonStatsVm {

    private val seasonStart = LocalDate.of(LocalDate.now().year, Month.MARCH, 1)

    private val allPlants = repo.observeAllPlants()
    private val allGardens = repo.gardens()
    private val allVarieties = referenceRepo.getAllVarieties()
    private val allCultures = referenceRepo.getAllCultures()
    private val allHarvests = repo.observeAllHarvests()
    private val allFertilizers = repo.observeAllFertilizerLogs()

    override val seasonSummary: StateFlow<SeasonSummary> = combine(
        allPlants, allHarvests, allFertilizers
    ) { plants, harvests, fertilizers ->
        val activePlantCount = plants.size
        val seasonHarvest = harvests.filter { it.date >= seasonStart }.sumOf { it.weightKg.toDouble() }.toFloat()
        val seasonTreatments = fertilizers.filter { it.date >= seasonStart }.size

        SeasonSummary(
            activePlants = activePlantCount,
            totalHarvest = seasonHarvest,
            totalTreatments = seasonTreatments
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeasonSummary())

    override val statsByCulture: StateFlow<List<CultureStats>> = combine(
        allPlants, allVarieties, allCultures, allHarvests, allFertilizers
    ) { plants, varieties, cultures, harvests, fertilizers ->
        val varietiesById = varieties.associateBy { it.id }
        val plantsWithCulture = plants.mapNotNull { plant ->
            varietiesById[plant.varietyId]?.let { variety ->
                Pair(plant, variety.cultureId)
            }
        }
        val cultureStats = mutableMapOf<String, CultureStats>()
        cultures.forEach { culture ->
            cultureStats[culture.id] = CultureStats(culture = culture)
        }

        plantsWithCulture.forEach { (plant, cultureId) ->
            val harvestSum = harvests.filter { it.plantId == plant.id && it.date >= seasonStart }.sumOf { it.weightKg.toDouble() }.toFloat()
            val fertilizerCount = fertilizers.filter { it.plantId == plant.id && it.date >= seasonStart }.size

            cultureStats.computeIfPresent(cultureId) { _, stats ->
                stats.copy(
                    totalHarvest = stats.totalHarvest + harvestSum,
                    totalFertilizer = stats.totalFertilizer + fertilizerCount
                )
            }
        }
        cultureStats.values.filter { it.totalHarvest > 0 || it.totalFertilizer > 0 }.sortedByDescending { it.totalHarvest }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    override val statsByGarden: StateFlow<List<GardenStats>> = combine(
        allPlants, allGardens, allHarvests
    ) { plants, gardens, harvests ->
        val plantsByGarden = plants.groupBy { it.gardenId }
        gardens.mapNotNull { garden ->
            val gardenPlants = plantsByGarden[garden.id] ?: return@mapNotNull null
            val totalHarvest = gardenPlants.sumOf { plant ->
                harvests.filter { it.plantId == plant.id && it.date >= seasonStart }.sumOf { it.weightKg.toDouble() }
            }.toFloat()

            if (totalHarvest > 0) GardenStats(garden, totalHarvest) else null
        }.sortedByDescending { it.totalHarvest }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
