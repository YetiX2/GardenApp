package com.example.gardenapp.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardVm @Inject constructor(repo: GardenRepository) : ViewModel() {
    // Теперь ViewModel предоставляет Flow с задачами, уже содержащими имена растений
    val pendingTasks = repo.pendingTasks()
}
