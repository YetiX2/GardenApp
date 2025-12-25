package com.example.gardenapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskInstanceEntity
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.repo.GardenRepository
import com.example.gardenapp.ui.dashboard.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TaskListVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {

    val allTasks = repo.allTasksWithPlantInfo()
    val allPlants = repo.observeAllPlants()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _taskToConfirmHarvest = MutableStateFlow<TaskInstanceEntity?>(null)
    val taskToConfirmHarvest = _taskToConfirmHarvest.asStateFlow()

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

    fun confirmHarvestAndCompleteTask(taskId: String, plantId: String, weight: Float, date: LocalDate, note: String?) {
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

    fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime, notes: String?, amount: Float?, unit: String?) {
        viewModelScope.launch {
            repo.addTask(plant, type, due, notes, amount, unit)
            _eventFlow.emit(UiEvent.ShowSnackbar("Задача добавлена"))
        }
    }
}
