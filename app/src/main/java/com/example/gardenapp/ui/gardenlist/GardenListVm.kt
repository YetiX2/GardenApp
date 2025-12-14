package com.example.gardenapp.ui.gardenlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GardenListVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    
    private val _gardens = MutableStateFlow<List<GardenEntity>>(emptyList())
    val gardens: StateFlow<List<GardenEntity>> = _gardens.asStateFlow()

    init {
        repo.gardens()
            .onEach { _gardens.value = it }
            .launchIn(viewModelScope)
    }

    suspend fun upsert(
        id: String?,
        name: String,
        w: Int, h: Int, step: Int,
        zone: Int?,
        type: GardenType,
        parentId: String?
    ): String {
        val newId = id ?: UUID.randomUUID().toString()
        val gardenToUpsert = (id?.let { repo.getGarden(it) } ?: GardenEntity(
            id = newId,
            name = name,
            widthCm = w,
            heightCm = h,
            gridStepCm = step,
            type = type,
            parentId = parentId,
            x = TODO(),
            y = TODO(),
            climateZone = zone
        )) // Get existing or create new
            .copy(
                name = name,
                widthCm = w,
                heightCm = h,
                gridStepCm = step,
                climateZone = zone, // CORRECTLY SETTING THE ZONE
                type = type,
                parentId = parentId
            )
        repo.upsertGarden(gardenToUpsert)
        return gardenToUpsert.id
    }

    fun delete(g: GardenEntity) = viewModelScope.launch {
        repo.deleteGarden(g)
    }
}
