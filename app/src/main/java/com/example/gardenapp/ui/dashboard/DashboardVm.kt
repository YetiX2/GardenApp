package com.example.gardenapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.WeatherRepository
import com.example.gardenapp.data.weather.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object PermissionDenied : WeatherUiState
    data class Success(val data: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

@HiltViewModel
class DashboardVm @Inject constructor(
    private val repo: GardenRepository,
    private val weatherRepo: WeatherRepository
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val allTasks = repo.allTasksWithPlantInfo()
    val gardens = repo.gardens()
    val recentActivity = repo.getRecentActivity()
    val allPlants = repo.observeAllPlants()

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            fetchWeather()
        } else {
            _weatherState.value = WeatherUiState.PermissionDenied
        }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            weatherRepo.getWeatherData()
                .onSuccess { _weatherState.value = WeatherUiState.Success(it) }
                .onFailure { _weatherState.value = WeatherUiState.Error(it.message ?: "Неизвестная ошибка") }
        }
    }

    fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime) {
        viewModelScope.launch {
            repo.addTask(plant, type, due)
            _eventFlow.emit(UiEvent.ShowSnackbar("Задача добавлена"))
        }
    }

    fun addFertilizerLog(plant: PlantEntity, grams: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addFertilizerLog(plant.id, date, grams, note)
            _eventFlow.emit(UiEvent.ShowSnackbar("Запись об удобрении добавлена"))
        }
    }

    fun addHarvestLog(plant: PlantEntity, weight: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addHarvestLog(plant.id, date, weight, note)
            _eventFlow.emit(UiEvent.ShowSnackbar("Запись об урожае добавлена"))
        }
    }

    fun createTestData() {
        viewModelScope.launch {
            repo.populateWithTestData()
        }
    }
}
