package com.example.gardenapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    // Expose all tasks now, not just pending
    val allTasks = repo.allTasksWithPlantInfo()

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            repo.setTaskStatus(taskId, newStatus)
        }
    }
}
