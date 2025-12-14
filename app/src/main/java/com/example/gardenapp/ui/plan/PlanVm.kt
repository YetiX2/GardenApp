package com.example.gardenapp.ui.plan

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlanVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceDao: ReferenceDao
) : ViewModel() {
    private val _currentGarden = mutableStateOf<GardenEntity?>(null)
    val currentGarden: State<GardenEntity?> = _currentGarden

    fun loadGarden(gardenId: String) {
        viewModelScope.launch {
            _currentGarden.value = repo.observeGardenById(gardenId).first()
        }
    }

    fun plantsFlow(gardenId: String): Flow<List<PlantEntity>> = repo.plants(gardenId)
    fun childGardensFlow(gardenId: String): Flow<List<GardenEntity>> = repo.getChildGardens(gardenId)
    
    suspend fun upsertPlant(p: PlantEntity) = repo.upsertPlant(p)
    suspend fun deletePlant(p: PlantEntity) = repo.deletePlant(p)
    suspend fun upsertGarden(g: GardenEntity) = repo.upsertGarden(g) // ADDED THIS

    fun fertilizerLogsFlow(plantId: String): Flow<List<FertilizerLogEntity>> = repo.fertilizerLogs(plantId)
    fun harvestLogsFlow(plantId: String): Flow<List<HarvestLogEntity>> = repo.harvestLogs(plantId)
    fun careRulesFlow(plantId: String): Flow<List<CareRuleEntity>> = repo.careRules(plantId)
    suspend fun addFertilizer(plantId: String, date: LocalDate, grams: Float, note: String?) = repo.addFertilizerLog(plantId, date, grams, note)
    suspend fun deleteFertilizer(item: FertilizerLogEntity) = repo.deleteFertilizerLog(item)
    suspend fun addHarvest(plantId: String, date: LocalDate, kg: Float, note: String?) = repo.addHarvestLog(plantId, date, kg, note)
    suspend fun deleteHarvest(item: HarvestLogEntity) = repo.deleteHarvestLog(item)
    suspend fun addCareRule(plantId: String, type: TaskType, start: LocalDate, everyDays: Int?, everyMonths: Int?) = repo.addCareRule(plantId, type, start, everyDays, everyMonths)
    suspend fun deleteCareRule(rule: CareRuleEntity) = repo.deleteCareRule(rule)

    val referenceGroups: Flow<List<ReferenceGroupEntity>> = referenceDao.getGroups()
    fun getCulturesByGroup(groupId: String): Flow<List<ReferenceCultureEntity>> = referenceDao.getCulturesByGroup(groupId)
    fun getVarietiesByCulture(cultureId: String): Flow<List<ReferenceVarietyEntity>> = referenceDao.getVarietiesByCulture(cultureId)
    fun getTagsForVariety(varietyId: String): Flow<List<ReferenceTagEntity>> = referenceDao.getTagsForVariety(varietyId)
    fun getAllCultures(): Flow<List<ReferenceCultureEntity>> = referenceDao.getAllCultures()
    fun getAllVarieties(): Flow<List<ReferenceVarietyEntity>> = referenceDao.getAllVarieties()
}
