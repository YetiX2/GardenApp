package com.example.gardenapp.ui.gardenlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.repo.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GardenListVm @Inject constructor(private val repo: GardenRepository) : ViewModel() {
    val gardens = repo.gardens()

    suspend fun upsert(
        id: String?,
        name: String,
        w: Int, h: Int, step: Int,
        zone: Int?,
        type: GardenType, // ADDED
        parentId: String? // ADDED
    ): String {
        val newId = id ?: UUID.randomUUID().toString()
        repo.upsertGarden(GardenEntity(newId, name, w, h, step, type, parentId, zone)) // MODIFIED
        return newId
    }

    fun delete(g: GardenEntity) = viewModelScope.launch {
        repo.deleteGarden(g)
    }
}
