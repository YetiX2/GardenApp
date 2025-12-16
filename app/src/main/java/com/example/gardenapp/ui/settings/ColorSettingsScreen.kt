package com.example.gardenapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor

private data class ColorPickerState(val label: String, val initialColor: Color, val onColorSelected: (Color) -> Unit)

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
    val defaultBackgroundColor = 0x4DFED8C0.toInt()
    val defaultTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val defaultSelectedStrokeColor = MaterialTheme.colorScheme.primary.toArgb()

    val plantColor by vm.settings.plantColor.collectAsState(initial = defaultPlantColor)
    val bedColor by vm.settings.bedColor.collectAsState(initial = defaultBedColor)
    val greenhouseColor by vm.settings.greenhouseColor.collectAsState(initial = defaultGreenhouseColor)
    val buildingColor by vm.settings.buildingColor.collectAsState(initial = defaultBuildingColor)
    val gridColor by vm.settings.gridColor.collectAsState(initial = defaultGridColor)
    val gardenBackgroundColor by vm.settings.gardenBackgroundColor.collectAsState(initial = defaultBackgroundColor)
    val textColor by vm.settings.textColor.collectAsState(initial = defaultTextColor)
    val selectedStrokeColor by vm.settings.selectedStrokeColor.collectAsState(initial = defaultSelectedStrokeColor)
    
    var showResetDialog by remember { mutableStateOf(false) }
    var colorPickerState by remember { mutableStateOf<ColorPickerState?>(null) }

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
                onClick = { color -> colorPickerState = ColorPickerState("Цвет растений", color) { vm.savePlantColor(it.toArgb()) } }
            )
            ColorSettingRow(
                label = "Цвет грядок",
                color = Color(bedColor ?: defaultBedColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет грядок", color) { vm.saveBedColor(it.toArgb()) } }
            )
            ColorSettingRow(
                label = "Цвет теплиц",
                color = Color(greenhouseColor ?: defaultGreenhouseColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет теплиц", color) { vm.saveGreenhouseColor(it.toArgb()) } }
            )
            ColorSettingRow(
                label = "Цвет строений",
                color = Color(buildingColor ?: defaultBuildingColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет строений", color) { vm.saveBuildingColor(it.toArgb()) } }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ColorSettingRow(
                label = "Цвет сетки",
                color = Color(gridColor ?: defaultGridColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет сетки", color) { vm.saveGridColor(it.toArgb()) } }
            )
            ColorSettingRow(
                label = "Цвет фона участка",
                color = Color(gardenBackgroundColor ?: defaultBackgroundColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет фона участка", color) { vm.saveGardenBackgroundColor(it.toArgb()) } }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ColorSettingRow(
                label = "Цвет текста",
                color = Color(textColor ?: defaultTextColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет текста", color) { vm.saveTextColor(it.toArgb()) } }
            )
            ColorSettingRow(
                label = "Цвет выделения",
                color = Color(selectedStrokeColor ?: defaultSelectedStrokeColor),
                onClick = { color -> colorPickerState = ColorPickerState("Цвет выделения", color) { vm.saveSelectedStrokeColor(it.toArgb()) } }
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Сбросить настройки") }
        }
    }

    if (showResetDialog) { // ADDED
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить настройки") },
            text = { Text("Вы уверены, что хотите сбросить все цветовые настройки до значений по умолчанию?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.resetColors()
                        showResetDialog = false
                    }
                ) { Text("Сбросить") }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Отмена") } }
        )
    }

    colorPickerState?.let { state ->
        var tempColor by remember { mutableStateOf(HsvColor.from(state.initialColor)) } 
        AlertDialog(
            onDismissRequest = { colorPickerState = null },
            title = { Text("Выберите цвет для \"${state.label}\"") },
            text = {
                ClassicColorPicker(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    color = tempColor,
                    onColorChanged = { hsvColor: HsvColor ->
                        tempColor = hsvColor
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.onColorSelected(tempColor.toColor())
                        colorPickerState = null
                    }
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { colorPickerState = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun ColorSettingRow(label: String, color: Color, onClick: (Color) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(color) },
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
