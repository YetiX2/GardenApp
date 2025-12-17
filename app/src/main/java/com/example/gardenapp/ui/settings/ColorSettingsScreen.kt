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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class ColorPickerState(val label: String, val initialColor: Color, val onColorSelected: (Color) -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    onBack: () -> Unit,
    vm: ColorSettingsVm = hiltViewModel()
) {
    val defaultPlantColor = 0xFF2E7D32.toInt()
    val defaultBedColor = 0x384CAF50.toInt()        // 0x38 = ~0.22 alpha
    val defaultGreenhouseColor = 0x38009688.toInt()
    val defaultBuildingColor = 0x998D6E63.toInt()   // чуть плотнее, здания
    val defaultGridColor = 0x339E9E9E.toInt()       // alpha ~0.2
    val defaultBackgroundColor = 0xFFF2F0EB.toInt()
    val defaultTextColor = 0xFF333333.toInt()
    val defaultSelectedStrokeColor = 0xFFFF9800.toInt()

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

            // ---- МИНИ-ПРЕВЬЮ ----
            Text(
                text = "Превью участка",
                style = MaterialTheme.typography.titleMedium
            )

            GardenColorsPreview(
                background = Color(gardenBackgroundColor ?: defaultBackgroundColor),
                grid = Color(gridColor ?: defaultGridColor),
                bed = Color(bedColor ?: defaultBedColor),
                greenhouse = Color(greenhouseColor ?: defaultGreenhouseColor),
                building = Color(buildingColor ?: defaultBuildingColor),
                plant = Color(plantColor ?: defaultPlantColor),
                selectedStroke = Color(selectedStrokeColor ?: defaultSelectedStrokeColor),
                text = Color(textColor ?: defaultTextColor),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))


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

@Composable
private fun GardenColorsPreview(
    background: Color,
    grid: Color,
    bed: Color,
    greenhouse: Color,
    building: Color,
    plant: Color,
    selectedStroke: Color,
    text: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // виртуальный "сад" 100x80 условных единиц
            val worldWidth = 100f
            val worldHeight = 80f

            val scaleX = size.width / worldWidth
            val scaleY = size.height / worldHeight
            val scale = minOf(scaleX, scaleY)

            fun w(x: Float) = x * scale
            fun worldToScreen(x: Float, y: Float) = Offset(w(x), w(y))

            // фон
            drawRect(
                color = background,
                topLeft = Offset.Zero,
                size = size
            )

            // сетка (крупная)
            val step = 10f
            val stepPx = w(step)
            if (stepPx >= 8f) {
                var x = 0f
                while (x <= size.width) {
                    drawLine(
                        color = grid,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height)
                    )
                    x += stepPx
                }
                var y = 0f
                while (y <= size.height) {
                    drawLine(
                        color = grid,
                        start = Offset(0f, y),
                        end = Offset(size.width, y)
                    )
                    y += stepPx
                }
            }

            // грядка
            val bedRect = Rect(
                worldToScreen(10f, 10f),
                worldToScreen(45f, 35f)
            )
            drawRect(bed, topLeft = bedRect.topLeft, size = bedRect.size)
            drawRect(Color.Black.copy(alpha = 0.4f), topLeft = bedRect.topLeft, size = bedRect.size, style = Stroke(1f))

            // теплица
            val greenhouseRect = Rect(
                worldToScreen(55f, 10f),
                worldToScreen(90f, 30f)
            )
            drawRect(greenhouse, topLeft = greenhouseRect.topLeft, size = greenhouseRect.size)
            drawRect(Color.Black.copy(alpha = 0.4f), topLeft = greenhouseRect.topLeft, size = greenhouseRect.size, style = Stroke(1f))

            // здание
            val buildingRect = Rect(
                worldToScreen(20f, 45f),
                worldToScreen(55f, 75f)
            )
            drawRect(building, topLeft = buildingRect.topLeft, size = buildingRect.size)
            drawRect(Color.Black.copy(alpha = 0.6f), topLeft = buildingRect.topLeft, size = buildingRect.size, style = Stroke(1.5f))

            // растения внутри грядки
            val plantPositions = listOf(
                Offset(20f, 18f),
                Offset(30f, 22f),
                Offset(40f, 28f),
            )
            plantPositions.forEachIndexed { index, posWorld ->
                val center = worldToScreen(posWorld.x, posWorld.y)
                val radius = w(2.5f)
                drawCircle(
                    color = plant,
                    radius = radius,
                    center = center
                )
                // одно "выделенное" растение
                if (index == 1) {
                    drawCircle(
                        color = selectedStroke,
                        radius = radius + w(1f),
                        center = center,
                        style = Stroke(width = w(0.5f))
                    )
                }
            }

            // подпись "Грядка"
            val bedLabel = textMeasurer.measure(
                text = "Грядка",
                style = TextStyle(fontSize = 10.sp, color = text)
            )
            drawText(
                bedLabel,
                topLeft = bedRect.center - Offset(bedLabel.size.width / 2f, bedLabel.size.height / 2f)
            )

            // подпись "Теплица"
            val greenhouseLabel = textMeasurer.measure(
                text = "Теплица",
                style = TextStyle(fontSize = 10.sp, color = text)
            )
            drawText(
                greenhouseLabel,
                topLeft = greenhouseRect.center - Offset(greenhouseLabel.size.width / 2f, greenhouseLabel.size.height / 2f)
            )

            // подпись "Дом"
            val buildingLabel = textMeasurer.measure(
                text = "Дом",
                style = TextStyle(fontSize = 10.sp, color = text)
            )
            drawText(
                buildingLabel,
                topLeft = buildingRect.center - Offset(buildingLabel.size.width / 2f, buildingLabel.size.height / 2f)
            )
        }
    }
}

