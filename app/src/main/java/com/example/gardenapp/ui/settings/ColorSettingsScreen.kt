package com.example.gardenapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.gardenapp.ui.DefaultColors
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor

private data class ColorPickerState(val label: String, val initialColor: Color, val onColorSelected: (Color) -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    onBack: () -> Unit,
    vm: ColorSettingsVm = hiltViewModel()
) {
    // Collect nullable Ints from the ViewModel
    val plantColorInt by vm.plantColor.collectAsState()
    val bedColorInt by vm.bedColor.collectAsState()
    val greenhouseColorInt by vm.greenhouseColor.collectAsState()
    val buildingColorInt by vm.buildingColor.collectAsState()
    val gridColorInt by vm.gridColor.collectAsState()
    val gardenBackgroundColorInt by vm.gardenBackgroundColor.collectAsState()
    val textColorInt by vm.textColor.collectAsState()
    val selectedStrokeColorInt by vm.selectedStrokeColor.collectAsState()
    
    // State for dialogs
    var showResetDialog by remember { mutableStateOf(false) }
    var colorPickerState by remember { mutableStateOf<ColorPickerState?>(null) }

    // Use the collected Int or the theme-aware default to create the final Color object
    val plantColor = Color(plantColorInt ?: DefaultColors.plantColor)
    val bedColor = Color(bedColorInt ?: DefaultColors.bedColor)
    val greenhouseColor = Color(greenhouseColorInt ?: DefaultColors.greenhouseColor)
    val buildingColor = Color(buildingColorInt ?: DefaultColors.buildingColor)
    val gridColor = Color(gridColorInt ?: DefaultColors.gridColor())
    val gardenBackgroundColor = Color(gardenBackgroundColorInt ?: DefaultColors.backgroundColor)
    val textColor = Color(textColorInt ?: DefaultColors.textColor())
    val selectedStrokeColor = Color(selectedStrokeColorInt ?: DefaultColors.selectedStrokeColor())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки цветов") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Назад") } }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorSettingRow("Цвет растений", plantColor) { color -> colorPickerState = ColorPickerState("Цвет растений", color) { vm.savePlantColor(it.toArgb()) } }
            ColorSettingRow("Цвет грядок", bedColor) { color -> colorPickerState = ColorPickerState("Цвет грядок", color) { vm.saveBedColor(it.toArgb()) } }
            ColorSettingRow("Цвет теплиц", greenhouseColor) { color -> colorPickerState = ColorPickerState("Цвет теплиц", color) { vm.saveGreenhouseColor(it.toArgb()) } }
            ColorSettingRow("Цвет строений", buildingColor) { color -> colorPickerState = ColorPickerState("Цвет строений", color) { vm.saveBuildingColor(it.toArgb()) } }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ColorSettingRow("Цвет сетки", gridColor) { color -> colorPickerState = ColorPickerState("Цвет сетки", color) { vm.saveGridColor(it.toArgb()) } }
            ColorSettingRow("Цвет фона участка", gardenBackgroundColor) { color -> colorPickerState = ColorPickerState("Цвет фона участка", color) { vm.saveGardenBackgroundColor(it.toArgb()) } }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ColorSettingRow("Цвет текста", textColor) { color -> colorPickerState = ColorPickerState("Цвет текста", color) { vm.saveTextColor(it.toArgb()) } }
            ColorSettingRow("Цвет выделения", selectedStrokeColor) { color -> colorPickerState = ColorPickerState("Цвет выделения", color) { vm.saveSelectedStrokeColor(it.toArgb()) } }
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

// Simplified signature for ColorSettingRow
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
