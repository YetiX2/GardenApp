package com.example.gardenapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gardenapp.data.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorSettingsVm @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _plantColor = MutableStateFlow<Int?>(null)
    val plantColor: StateFlow<Int?> = _plantColor.asStateFlow()
    private val _bedColor = MutableStateFlow<Int?>(null)
    val bedColor: StateFlow<Int?> = _bedColor.asStateFlow()
    private val _greenhouseColor = MutableStateFlow<Int?>(null)
    val greenhouseColor: StateFlow<Int?> = _greenhouseColor.asStateFlow()
    private val _buildingColor = MutableStateFlow<Int?>(null)
    val buildingColor: StateFlow<Int?> = _buildingColor.asStateFlow()
    private val _gridColor = MutableStateFlow<Int?>(null)
    val gridColor: StateFlow<Int?> = _gridColor.asStateFlow()
    private val _gardenBackgroundColor = MutableStateFlow<Int?>(null)
    val gardenBackgroundColor: StateFlow<Int?> = _gardenBackgroundColor.asStateFlow()
    private val _textColor = MutableStateFlow<Int?>(null)
    val textColor: StateFlow<Int?> = _textColor.asStateFlow()
    private val _selectedStrokeColor = MutableStateFlow<Int?>(null)
    val selectedStrokeColor: StateFlow<Int?> = _selectedStrokeColor.asStateFlow()

    init {
        settingsManager.plantColor.onEach { _plantColor.value = it }.launchIn(viewModelScope)
        settingsManager.bedColor.onEach { _bedColor.value = it }.launchIn(viewModelScope)
        settingsManager.greenhouseColor.onEach { _greenhouseColor.value = it }.launchIn(viewModelScope)
        settingsManager.buildingColor.onEach { _buildingColor.value = it }.launchIn(viewModelScope)
        settingsManager.gridColor.onEach { _gridColor.value = it }.launchIn(viewModelScope)
        settingsManager.gardenBackgroundColor.onEach { _gardenBackgroundColor.value = it }.launchIn(viewModelScope)
        settingsManager.textColor.onEach { _textColor.value = it }.launchIn(viewModelScope)
        settingsManager.selectedStrokeColor.onEach { _selectedStrokeColor.value = it }.launchIn(viewModelScope)
    }

    fun savePlantColor(color: Int) = viewModelScope.launch { settingsManager.savePlantColor(color) }
    fun saveBedColor(color: Int) = viewModelScope.launch { settingsManager.saveBedColor(color) }
    fun saveGreenhouseColor(color: Int) = viewModelScope.launch { settingsManager.saveGreenhouseColor(color) }
    fun saveBuildingColor(color: Int) = viewModelScope.launch { settingsManager.saveBuildingColor(color) }
    fun saveGridColor(color: Int) = viewModelScope.launch { settingsManager.saveGridColor(color) }
    fun saveGardenBackgroundColor(color: Int) = viewModelScope.launch { settingsManager.saveGardenBackgroundColor(color) }
    fun saveTextColor(color: Int) = viewModelScope.launch { settingsManager.saveTextColor(color) }
    fun saveSelectedStrokeColor(color: Int) = viewModelScope.launch { settingsManager.saveSelectedStrokeColor(color) }

    fun resetColors() = viewModelScope.launch {
        settingsManager.clearAllColors()
    }
}
