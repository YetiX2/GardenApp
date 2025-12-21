package com.example.gardenapp.ui.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GardenPlanVm @Inject constructor(
    private val repo: GardenRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gardenId: String = savedStateHandle.get<String>("gardenId")!!

    val garden: StateFlow<GardenEntity?> = repo.observeGardenById(gardenId) // FIXED THIS
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val plants: StateFlow<List<PlantEntity>> = repo.plantsForGardens(gardenId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val childGardens: StateFlow<List<GardenEntity>> = repo.getChildGardens(gardenId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ... methods to add/update plants on the plan will go here ...
}
