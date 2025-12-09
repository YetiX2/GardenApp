package com.example.gardenapp.ui.gardenlist

import androidx.lifecycle.ViewModel
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GardenListVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    val gardens = repo.gardens()
    
    suspend fun upsert(id: String?, name: String, w: Int, h: Int, step: Int, zone: Int?): String {
        val entityId = id ?: UUID.randomUUID().toString()
        val entity = GardenEntity(
            id = entityId,
            name = name,
            widthCm = w,
            heightCm = h,
            gridStepCm = step,
            climateZone = zone
        )
        repo.upsertGarden(entity)
        return entityId
    }
    
    suspend fun delete(g: GardenEntity) = repo.deleteGarden(g)
}
