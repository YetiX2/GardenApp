package com.example.gardenapp.ui.plant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.ReferenceDataRepository
import com.example.gardenapp.ui.dashboard.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlantEditorVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceRepo: ReferenceDataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val plantId: String = savedStateHandle.get<String>("plantId")!!

    val plant = repo.observePlant(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val varietyDetails: StateFlow<ReferenceVarietyEntity?> = plant.flatMapLatest { p ->
        p?.varietyId?.let { referenceRepo.getVariety(it) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val culture: StateFlow<ReferenceCultureEntity?> = varietyDetails.flatMapLatest { variety ->
        variety?.cultureId?.let { referenceRepo.getCulture(it) } ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val varietyTags: StateFlow<List<ReferenceTagEntity>> = plant.flatMapLatest { p ->
        p?.varietyId?.let { referenceRepo.getTagsForVariety(it) } ?: flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: Flow<List<TaskWithPlantInfo>> = repo.observeTasksForPlant(plantId)

    val fertilizerLogs: Flow<List<FertilizerLogEntity>> = repo.fertilizerLogs(plantId)
    val harvestLogs: Flow<List<HarvestLogEntity>> = repo.harvestLogs(plantId)
    val careRules: Flow<List<CareRuleEntity>> = repo.careRules(plantId)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            repo.setTaskStatus(taskId, newStatus)
        }
    }

    fun addFertilizerLog(grams: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addFertilizerLog(plantId, date, grams, note)
            _eventFlow.emit(UiEvent.ShowSnackbar("Запись об удобрении добавлена"))
        }
    }

    fun addHarvestLog(weight: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addHarvestLog(plantId, date, weight, note)
            _eventFlow.emit(UiEvent.ShowSnackbar("Запись об урожае добавлена"))
        }
    }

    fun addCareRule(type: TaskType, startDate: LocalDate, everyDays: Int) {
        viewModelScope.launch {
            repo.addCareRule(plantId, type, startDate, everyDays, null)
            _eventFlow.emit(UiEvent.ShowSnackbar("Правило ухода добавлено"))
        }
    }

    fun deleteFertilizerLog(item: FertilizerLogEntity) = viewModelScope.launch { repo.deleteFertilizerLog(item) }
    fun deleteHarvestLog(item: HarvestLogEntity) = viewModelScope.launch { repo.deleteHarvestLog(item) }
    fun deleteCareRule(item: CareRuleEntity) = viewModelScope.launch { repo.deleteCareRule(item) }
}
