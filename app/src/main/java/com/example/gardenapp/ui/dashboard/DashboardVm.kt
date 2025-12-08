package com.example.gardenapp.ui.dashboard

import androidx.lifecycle.ViewModel
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    val pendingTasks = repo.pendingTasks()
}
