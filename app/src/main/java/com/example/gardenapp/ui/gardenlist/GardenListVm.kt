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
        val idToUse = id ?: UUID.randomUUID().toString()
        
        val existingGarden = id?.let { repo.getGarden(it) }
        val gardenToUpsert = (existingGarden ?: GardenEntity(id = idToUse, name = name, widthCm = w, heightCm = h, gridStepCm = step, climateZone = zone))
            .copy(
                name = name,
                widthCm = w,
                heightCm = h,
                gridStepCm = step,
                type = type,
                parentId = parentId,
                climateZone = zone
            )

        repo.upsertGarden(gardenToUpsert)
        return idToUse
    }

    fun delete(g: GardenEntity) = viewModelScope.launch {
        repo.deleteGarden(g)
    }
}
