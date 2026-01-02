package com.example.gardenapp.ui.plant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.*
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.ReferenceDataRepository
import com.example.gardenapp.data.settings.SettingsManager
import com.example.gardenapp.ui.dashboard.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlantEditorVm @Inject constructor(
    private val repo: GardenRepository,
    private val referenceRepo: ReferenceDataRepository,
    private val settingsManager: SettingsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val plantId: String = savedStateHandle.get<String>("plantId")!!

    val plant = repo.observePlant(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastUsedGroupId = settingsManager.lastUsedGroupId.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val lastUsedCultureId = settingsManager.lastUsedCultureId.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _taskToConfirmHarvest = MutableStateFlow<TaskInstanceEntity?>(null)
    val taskToConfirmHarvest: StateFlow<TaskInstanceEntity?> = _taskToConfirmHarvest.asStateFlow()

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

    fun saveLastUsedIds(groupId: String, cultureId: String) {
        viewModelScope.launch {
            settingsManager.setLastUsedIds(groupId, cultureId)
        }
    }

    fun addTask(type: TaskType, due: LocalDateTime, notes: String?, amount: Float?, unit: String?) {
        viewModelScope.launch {
            plant.value?.let {
                repo.addTask(it, type, due, notes, amount, unit)
                _eventFlow.emit(UiEvent.ShowSnackbar("Задача добавлена"))
            }
        }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            val task = repo.getTask(taskId)
            if (task != null) {
                when (task.type) {
                    TaskType.HARVEST -> if (newStatus == TaskStatus.DONE) _taskToConfirmHarvest.value = task else repo.setTaskStatus(taskId, newStatus)
                    TaskType.FERTILIZE -> {
                        if (newStatus == TaskStatus.DONE) {
                            repo.addFertilizerLog(task.plantId, LocalDate.now(), task.amount ?: 0f, "Автоматически из задачи")
                        }
                        repo.setTaskStatus(taskId, newStatus)
                    }
                    else -> repo.setTaskStatus(taskId, newStatus)
                }
            }
        }
    }

    fun confirmHarvestAndCompleteTask(taskId: String, weight: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addHarvestLog(plantId, date, weight, note)
            repo.setTaskStatus(taskId, TaskStatus.DONE)
            _taskToConfirmHarvest.value = null
            _eventFlow.emit(UiEvent.ShowSnackbar("Урожай собран, задача выполнена"))
        }
    }

    fun dismissHarvestConfirmation() {
        _taskToConfirmHarvest.value = null
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

    fun addCareRule(
        type: TaskType,
        everyDays: Int,
        note: String?,
        amount: Float?,
        unit: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ) {
        viewModelScope.launch {
            repo.addCareRule(plantId, type, LocalDate.now(), startDate, endDate, everyDays, null, note, amount, unit)
            _eventFlow.emit(UiEvent.ShowSnackbar("Правило ухода добавлено"))
        }
    }
    fun updateCareRule(rule: CareRuleEntity) {
        viewModelScope.launch {
            repo.updateCareRule(rule)
            _eventFlow.emit(UiEvent.ShowSnackbar("Правило ухода обновлено"))
        }
    }
    fun deleteFertilizerLog(item: FertilizerLogEntity) = viewModelScope.launch { repo.deleteFertilizerLog(item) }
    fun deleteHarvestLog(item: HarvestLogEntity) = viewModelScope.launch { repo.deleteHarvestLog(item) }
    fun deleteCareRule(item: CareRuleEntity) = viewModelScope.launch { repo.deleteCareRule(item) }
}
