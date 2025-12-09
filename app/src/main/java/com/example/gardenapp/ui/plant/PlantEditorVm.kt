package com.example.gardenapp.ui.plant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.CareRuleEntity
import com.example.gardenapp.data.db.FertilizerLogEntity
import com.example.gardenapp.data.db.HarvestLogEntity
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.ui.dashboard.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlantEditorVm @Inject constructor(
    private val repo: GardenRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val plantId: String = savedStateHandle.get<String>("plantId")!!

    val plant = repo.observePlant(plantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fertilizerLogs: Flow<List<FertilizerLogEntity>> = repo.fertilizerLogs(plantId)
    val harvestLogs: Flow<List<HarvestLogEntity>> = repo.harvestLogs(plantId)
    val careRules: Flow<List<CareRuleEntity>> = repo.careRules(plantId)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
