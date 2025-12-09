package com.example.gardenapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    val allTasks = repo.allTasksWithPlantInfo()
    val gardens = repo.gardens()
    val recentActivity = repo.getRecentActivity()
    val allPlants = repo.observeAllPlants()

    fun addTask(plant: PlantEntity, type: TaskType, due: LocalDateTime) {
        viewModelScope.launch {
            repo.addTask(plant, type, due)
        }
    }

    fun addFertilizerLog(plant: PlantEntity, grams: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addFertilizerLog(plant.id, date, grams, note)
        }
    }

    fun addHarvestLog(plant: PlantEntity, weight: Float, date: LocalDate, note: String?) {
        viewModelScope.launch {
            repo.addHarvestLog(plant.id, date, weight, note)
        }
    }

    fun createTestData() {
        viewModelScope.launch {
            repo.populateWithTestData()
        }
    }
}
