package com.example.gardenapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.TestDataGenerator
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.data.repo.WeatherRepository
import com.example.gardenapp.worker.CareTaskWorker
import com.example.gardenapp.data.weather.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
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

data class SeasonSummary( 
    val activePlants: Int = 0,
    val totalHarvest: Float = 0f,
    val totalTreatments: Int = 0
)

@HiltViewModel
class DashboardVm @Inject constructor(
    private val application: Application,
    private val repo: GardenRepository,
    private val weatherRepo: WeatherRepository,
    private val testDataGenerator: TestDataGenerator
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val allTasks = repo.allTasksWithPlantInfo()
    val gardens = repo.gardens()
    val recentActivity = repo.getRecentActivity()
    val allPlants = repo.observeAllPlants()

    // ADDED SEASON SUMMARY
    val seasonSummary: StateFlow<SeasonSummary> = combine(
        allPlants, repo.observeAllHarvests(), repo.observeAllFertilizerLogs()
    ) { plants, harvests, fertilizers ->
        val seasonStart = LocalDate.of(LocalDate.now().year, Month.MARCH, 1)
        val activePlantCount = plants.size
        val seasonHarvest = harvests.filter { it.date >= seasonStart }.sumOf { it.weightKg.toDouble() }.toFloat()
        val seasonTreatments = fertilizers.filter { it.date >= seasonStart }.size

        SeasonSummary(
            activePlants = activePlantCount,
            totalHarvest = seasonHarvest,
            totalTreatments = seasonTreatments
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeasonSummary())

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            fetchWeather()
        } else {
            _weatherState.value = WeatherUiState.PermissionDenied
        }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Show full-screen loading only on initial fetch
            if (weatherState.value !is WeatherUiState.Success) {
                _weatherState.value = WeatherUiState.Loading
            }
            weatherRepo.getWeatherData()
                .onSuccess { response ->
                    _weatherState.value = WeatherUiState.Success(response)
                }
                .onFailure { error ->
                    // Don't show error over existing successful data during a refresh
                    if (weatherState.value !is WeatherUiState.Success) {
                        _weatherState.value = WeatherUiState.Error(error.message ?: "Неизвестная ошибка")
                    }
                }
            _isRefreshing.value = false
        }
    }

    fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime, notes: String?, amount: Float?, unit: String?) {
        viewModelScope.launch {
            repo.addTask(plant, type, due, notes, amount, unit)
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
            withContext(Dispatchers.IO) { 
                testDataGenerator.seed()
            }
            _eventFlow.emit(UiEvent.ShowSnackbar("Тестовые данные созданы"))
        }
    }
    fun runCareTaskWorkerNow() { // ADDED THIS
        val workRequest = OneTimeWorkRequestBuilder<CareTaskWorker>().build()
        WorkManager.getInstance(application).enqueue(workRequest)
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Запуск фоновой проверки правил..."))
        }
    }
}
