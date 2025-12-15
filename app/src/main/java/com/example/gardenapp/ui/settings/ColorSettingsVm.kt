package com.example.gardenapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.repo.ColorSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorSettingsVm @Inject constructor(
    val settings: ColorSettingsRepository
) : ViewModel() {

    fun savePlantColor(color: Int) = viewModelScope.launch {
        settings.savePlantColor(color)
    }

    fun saveBedColor(color: Int) = viewModelScope.launch {
        settings.saveBedColor(color)
    }

    fun saveGreenhouseColor(color: Int) = viewModelScope.launch {
        settings.saveGreenhouseColor(color)
    }

    fun saveBuildingColor(color: Int) = viewModelScope.launch {
        settings.saveBuildingColor(color)
    }

    fun saveGridColor(color: Int) = viewModelScope.launch {
        settings.saveGridColor(color)
    }

    fun saveGardenBackgroundColor(color: Int) = viewModelScope.launch {
        settings.saveGardenBackgroundColor(color)
    }

    fun saveTextColor(color: Int) = viewModelScope.launch {
        settings.saveTextColor(color)
    }

    fun saveSelectedStrokeColor(color: Int) = viewModelScope.launch {
        settings.saveSelectedStrokeColor(color)
    }

    fun resetColors() = viewModelScope.launch { // ADDED
        settings.clearAllColors()
    }
}
