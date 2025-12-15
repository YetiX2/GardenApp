package com.example.gardenapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    onBack: () -> Unit,
    vm: ColorSettingsVm = hiltViewModel()
) {
    val defaultPlantColor = 0xFF4CAF50.toInt()
    val defaultBedColor = 0x99668B7E.toInt()
    val defaultGreenhouseColor = 0x99D1C4E9.toInt()
    val defaultBuildingColor = 0x99C2DEDC.toInt()
    val defaultGridColor = 0x4D1C1B1F.toInt()
    val defaultBackgroundColor = 0 // Transparent

    val plantColor by vm.settings.plantColor.collectAsState(initial = defaultPlantColor)
    val bedColor by vm.settings.bedColor.collectAsState(initial = defaultBedColor)
    val greenhouseColor by vm.settings.greenhouseColor.collectAsState(initial = defaultGreenhouseColor)
    val buildingColor by vm.settings.buildingColor.collectAsState(initial = defaultBuildingColor)
    val gridColor by vm.settings.gridColor.collectAsState(initial = defaultGridColor)
    val gardenBackgroundColor by vm.settings.gardenBackgroundColor.collectAsState(initial = defaultBackgroundColor)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки цветов") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorSettingRow(
                label = "Цвет растений",
                color = Color(plantColor ?: defaultPlantColor),
                onColorSelected = { newColor -> vm.savePlantColor(newColor.toArgb()) }
            )
            ColorSettingRow(
                label = "Цвет грядок",
                color = Color(bedColor ?: defaultBedColor),
                onColorSelected = { newColor -> vm.saveBedColor(newColor.toArgb()) }
            )
            ColorSettingRow(
                label = "Цвет теплиц",
                color = Color(greenhouseColor ?: defaultGreenhouseColor),
                onColorSelected = { newColor -> vm.saveGreenhouseColor(newColor.toArgb()) }
            )
            ColorSettingRow(
                label = "Цвет строений",
                color = Color(buildingColor ?: defaultBuildingColor),
                onColorSelected = { newColor -> vm.saveBuildingColor(newColor.toArgb()) }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ColorSettingRow(
                label = "Цвет сетки",
                color = Color(gridColor ?: defaultGridColor),
                onColorSelected = { newColor -> vm.saveGridColor(newColor.toArgb()) }
            )
            ColorSettingRow(
                label = "Цвет фона участка",
                color = Color(gardenBackgroundColor ?: defaultBackgroundColor),
                onColorSelected = { newColor -> vm.saveGardenBackgroundColor(newColor.toArgb()) }
            )
        }
    }
}

@Composable
private fun ColorSettingRow(label: String, color: Color, onColorSelected: (Color) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { 
            // TODO: Open a real color picker dialog here
            val newColor = when(color.toArgb()) { // Compare ARGB values
                Color.Red.toArgb() -> Color.Green
                Color.Green.toArgb() -> Color.Blue
                Color.Blue.toArgb() -> Color.Yellow
                else -> Color.Red
            }
            onColorSelected(newColor)
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(String.format("#%08X", color.toArgb()), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(24.dp).background(color))
        }
    }
}
