package com.example.gardenapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.repo.ColorSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorSettingsVm @Inject constructor(
    val settings: ColorSettingsRepository
) : ViewModel() {

    // --- Флаг "отдельные палитры для тёмной темы" ---

    val useSeparateDarkPalette: Flow<Boolean> = settings.useSeparateDarkPalette

    fun setUseSeparateDarkPalette(value: Boolean) = viewModelScope.launch {
        settings.setUseSeparateDarkPalette(value)
    }

    // --- LIGHT palette save ---

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

    // --- DARK palette save ---

    fun savePlantColorDark(color: Int) = viewModelScope.launch {
        settings.savePlantColorDark(color)
    }

    fun saveBedColorDark(color: Int) = viewModelScope.launch {
        settings.saveBedColorDark(color)
    }

    fun saveGreenhouseColorDark(color: Int) = viewModelScope.launch {
        settings.saveGreenhouseColorDark(color)
    }

    fun saveBuildingColorDark(color: Int) = viewModelScope.launch {
        settings.saveBuildingColorDark(color)
    }

    fun saveGridColorDark(color: Int) = viewModelScope.launch {
        settings.saveGridColorDark(color)
    }

    fun saveGardenBackgroundColorDark(color: Int) = viewModelScope.launch {
        settings.saveGardenBackgroundColorDark(color)
    }

    fun saveTextColorDark(color: Int) = viewModelScope.launch {
        settings.saveTextColorDark(color)
    }

    fun saveSelectedStrokeColorDark(color: Int) = viewModelScope.launch {
        settings.saveSelectedStrokeColorDark(color)
    }

    // --- RESET ---

    fun resetColors() = viewModelScope.launch {
        settings.clearAllColors()
    }
}
