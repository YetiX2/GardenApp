package com.example.gardenapp.ui.plan

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.ColorSettingsRepository
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlanVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceDao: ReferenceDao,
    private val colorSettingsRepo: ColorSettingsRepository
) : ViewModel() {
    private val _currentGarden = mutableStateOf<GardenEntity?>(null)
    val currentGarden: State<GardenEntity?> = _currentGarden

    // --- Color State ---
    private val _plantColor = MutableStateFlow(0xFF4CAF50.toInt())
    val plantColor: StateFlow<Int> = _plantColor.asStateFlow()

    private val _bedColor = MutableStateFlow(0x99668B7E.toInt())
    val bedColor: StateFlow<Int> = _bedColor.asStateFlow()

    private val _greenhouseColor = MutableStateFlow(0x99D1C4E9.toInt())
    val greenhouseColor: StateFlow<Int> = _greenhouseColor.asStateFlow()

    private val _buildingColor = MutableStateFlow(0x99C2DEDC.toInt())
    val buildingColor: StateFlow<Int> = _buildingColor.asStateFlow()

    private val _gridColor = MutableStateFlow(0x4D1C1B1F.toInt()) // outline with 0.3f alpha
    val gridColor: StateFlow<Int> = _gridColor.asStateFlow()

    private val _gardenBackgroundColor = MutableStateFlow(0)
    val gardenBackgroundColor: StateFlow<Int> = _gardenBackgroundColor.asStateFlow()


    private val _gardenTextColor = MutableStateFlow(0)
    val textColor: StateFlow<Int> = _gardenTextColor.asStateFlow()


    private val _selectedStrokeColor = MutableStateFlow(0)
    val selectedStrokeColor: StateFlow<Int> = _selectedStrokeColor.asStateFlow()


    init {
        // Collect colors from repository and update local state
        colorSettingsRepo.plantColor.onEach { _plantColor.value = it ?: 0xFF4CAF50.toInt() }.launchIn(viewModelScope)
        colorSettingsRepo.bedColor.onEach { _bedColor.value = it ?: 0x99668B7E.toInt() }.launchIn(viewModelScope)
        colorSettingsRepo.greenhouseColor.onEach { _greenhouseColor.value = it ?: 0x99D1C4E9.toInt() }.launchIn(viewModelScope)
        colorSettingsRepo.buildingColor.onEach { _buildingColor.value = it ?: 0x99C2DEDC.toInt() }.launchIn(viewModelScope)
        colorSettingsRepo.gridColor.onEach { _gridColor.value = it ?: 0x4D1C1B1F.toInt() }.launchIn(viewModelScope)
        colorSettingsRepo.gardenBackgroundColor.onEach { _gardenBackgroundColor.value = it ?: 0 }.launchIn(viewModelScope)
        colorSettingsRepo.textColor.onEach { _gardenTextColor.value = it ?: Color.Black.toArgb() }.launchIn(viewModelScope)
        colorSettingsRepo.selectedStrokeColor.onEach { _selectedStrokeColor.value = it ?:Color.Black.toArgb()  }.launchIn(viewModelScope)

    }


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
