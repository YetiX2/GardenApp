package com.example.gardenapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    val pendingTasks = repo.pendingTasks()

    fun createTestData() {
        viewModelScope.launch {
            repo.populateWithTestData()
        }
    }
}
