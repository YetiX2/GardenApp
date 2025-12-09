package com.example.gardenapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TaskListVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    val allTasks = repo.allTasksWithPlantInfo()
    val allPlants = repo.observeAllPlants() // For the dialog

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            repo.setTaskStatus(taskId, newStatus)
        }
    }

    fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime) {
        viewModelScope.launch {
            repo.addTask(plant, type, due)
        }
    }
}
