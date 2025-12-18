package com.example.gardenapp.ui.settings

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
//import com.godaddy.android.colorpicker.toColor

private data class ColorPickerState(
    val label: String,
    val initialColor: Color,
    val onColorSelected: (Color) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSettingsScreen(
    onBack: () -> Unit,
    vm: ColorSettingsVm = hiltViewModel()
) {
    // --- дефолты для LIGHT палитры ---
    val defaultPlantColorLight = 0xFF2E7D32.toInt()
    val defaultBedColorLight = 0x384CAF50.toInt()
    val defaultGreenhouseColorLight = 0x38009688.toInt()
    val defaultBuildingColorLight = 0x998D6E63.toInt()
    val defaultGridColorLight = 0x339E9E9E.toInt()
    val defaultBackgroundColorLight = 0xFFF2F0EB.toInt()
    val defaultTextColorLight = 0xFF333333.toInt()
    val defaultSelectedStrokeColorLight = 0xFFFF9800.toInt()

    // --- дефолты для DARK палитры ---
    val defaultPlantColorDark = 0xFF81C784.toInt()
    val defaultBedColorDark = 0x664CAF50.toInt()
    val defaultGreenhouseColorDark = 0x662BBBAD.toInt()
    val defaultBuildingColorDark = 0xFF4E342E.toInt()
    val defaultGridColorDark = 0x66B0BEC5.toInt()
    val defaultBackgroundColorDark = 0xFF121212.toInt()
    val defaultTextColorDark = 0xFFECEFF1.toInt()
    val defaultSelectedStrokeColorDark = 0xFFFFB74D.toInt()

    // --- читаем флаг разделения палитр ---
    val useSeparateDark by vm.useSeparateDarkPalette.collectAsState(initial = false)

    // --- читаем LIGHT цвета из репозитория ---
    val plantColorLight by vm.settings.plantColor.collectAsState(initial = defaultPlantColorLight)
    val bedColorLight by vm.settings.bedColor.collectAsState(initial = defaultBedColorLight)
    val greenhouseColorLight by vm.settings.greenhouseColor.collectAsState(initial = defaultGreenhouseColorLight)
    val buildingColorLight by vm.settings.buildingColor.collectAsState(initial = defaultBuildingColorLight)
    val gridColorLight by vm.settings.gridColor.collectAsState(initial = defaultGridColorLight)
    val backgroundColorLight by vm.settings.gardenBackgroundColor.collectAsState(initial = defaultBackgroundColorLight)
    val textColorLight by vm.settings.textColor.collectAsState(initial = defaultTextColorLight)
    val selectedStrokeColorLight by vm.settings.selectedStrokeColor.collectAsState(initial = defaultSelectedStrokeColorLight)

    // --- читаем DARK цвета ---
    val plantColorDark by vm.settings.plantColorDark.collectAsState(initial = defaultPlantColorDark)
    val bedColorDark by vm.settings.bedColorDark.collectAsState(initial = defaultBedColorDark)
    val greenhouseColorDark by vm.settings.greenhouseColorDark.collectAsState(initial = defaultGreenhouseColorDark)
    val buildingColorDark by vm.settings.buildingColorDark.collectAsState(initial = defaultBuildingColorDark)
    val gridColorDark by vm.settings.gridColorDark.collectAsState(initial = defaultGridColorDark)
    val backgroundColorDark by vm.settings.gardenBackgroundColorDark.collectAsState(initial = defaultBackgroundColorDark)
    val textColorDark by vm.settings.textColorDark.collectAsState(initial = defaultTextColorDark)
    val selectedStrokeColorDark by vm.settings.selectedStrokeColorDark.collectAsState(initial = defaultSelectedStrokeColorDark)

    var showResetDialog by remember { mutableStateOf(false) }
    var colorPickerState by remember { mutableStateOf<ColorPickerState?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки цветов плана") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState()) // ADDED
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- переключатель раздельных палитр ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Разные цвета для тёмной темы")
                Switch(
                    checked = useSeparateDark,
                    onCheckedChange = { checked ->
                        vm.setUseSeparateDarkPalette(checked)
                    }
                )

            }

            Spacer(Modifier.height(8.dp))

            // ---- LIGHT / СВЕТЛАЯ ТЕМА ----
            Text("Светлая тема", style = MaterialTheme.typography.titleMedium)

            GardenColorsPreview(
                background = Color(backgroundColorLight?:defaultBackgroundColorLight),
                grid = Color(gridColorLight?:defaultGridColorLight),
                bed = Color(bedColorLight?:defaultBedColorLight),
                greenhouse = Color(greenhouseColorLight?:defaultGreenhouseColorLight),
                building = Color(buildingColorLight?: defaultBuildingColorLight),
                plant = Color(plantColorLight?: defaultPlantColorLight),
                selectedStroke = Color(selectedStrokeColorLight?:defaultSelectedStrokeColorLight),
                text = Color(textColorLight?:defaultTextColorLight),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            ColorSettingRow(
                label = "Цвет растений",
                color = Color(plantColorLight?:defaultPlantColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет растений (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.savePlantColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет грядок",
                color = Color(bedColorLight?:defaultBedColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет грядок (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveBedColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет теплиц",
                color = Color(greenhouseColorLight?:defaultGreenhouseColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет теплиц (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveGreenhouseColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет строений",
                color = Color(buildingColorLight?:defaultBuildingColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет строений (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveBuildingColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет сетки",
                color = Color(gridColorLight?:defaultGridColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет сетки (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveGridColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет фона участка",
                color = Color(backgroundColorLight?:defaultBackgroundColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет фона участка (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveGardenBackgroundColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет текста",
                color = Color(textColorLight?:defaultTextColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет текста (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveTextColor(selected.toArgb())
                }
            }

            ColorSettingRow(
                label = "Цвет выделения",
                color = Color(selectedStrokeColorLight?:defaultSelectedStrokeColorLight),
            ) { color ->
                colorPickerState = ColorPickerState(
                    label = "Цвет выделения (светлая тема)",
                    initialColor = color
                ) { selected ->
                    vm.saveSelectedStrokeColor(selected.toArgb())
                }
            }

            // ---- DARK / ТЁМНАЯ ТЕМА ----
            if (useSeparateDark) {
                Spacer(Modifier.height(16.dp))
                androidx.compose.material3.Divider()
                Spacer(Modifier.height(8.dp))

                Text("Тёмная тема", style = MaterialTheme.typography.titleMedium)

                GardenColorsPreview(
                    background = Color(backgroundColorDark?:defaultBackgroundColorDark),
                    grid = Color(gridColorDark?:defaultGridColorDark),
                    bed = Color(bedColorDark?:defaultBedColorDark),
                    greenhouse = Color(greenhouseColorDark?:defaultGreenhouseColorDark),
                    building = Color(buildingColorDark?:defaultBuildingColorDark),
                    plant = Color(plantColorDark?:defaultPlantColorDark),
                    selectedStroke = Color(selectedStrokeColorDark?:defaultSelectedStrokeColorDark),
                    text = Color(textColorDark?:defaultTextColorDark),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                ColorSettingRow(
                    label = "Цвет растений (тёмная тема)",
                    color = Color(plantColorDark?:defaultPlantColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет растений (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.savePlantColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет грядок (тёмная тема)",
                    color = Color(bedColorDark?:defaultBedColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет грядок (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveBedColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет теплиц (тёмная тема)",
                    color = Color(greenhouseColorDark?:defaultGreenhouseColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет теплиц (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveGreenhouseColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет строений (тёмная тема)",
                    color = Color(buildingColorDark?:defaultBuildingColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет строений (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveBuildingColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет сетки (тёмная тема)",
                    color = Color(gridColorDark?:defaultGridColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет сетки (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveGridColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет фона участка (тёмная тема)",
                    color = Color(backgroundColorDark?:defaultBackgroundColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет фона участка (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveGardenBackgroundColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет текста (тёмная тема)",
                    color = Color(textColorDark?:defaultTextColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет текста (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveTextColorDark(selected.toArgb())
                    }
                }

                ColorSettingRow(
                    label = "Цвет выделения (тёмная тема)",
                    color = Color(selectedStrokeColorDark?:defaultSelectedStrokeColorDark),
                ) { color ->
                    colorPickerState = ColorPickerState(
                        label = "Цвет выделения (тёмная тема)",
                        initialColor = color
                    ) { selected ->
                        vm.saveSelectedStrokeColorDark(selected.toArgb())
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить настройки")
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Сбросить настройки") },
            text = { Text("Сбросить все цветовые настройки до значений по умолчанию (для обеих тем)?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.resetColors()
                        showResetDialog = false
                    }
                ) {
                    Text("Сбросить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    colorPickerState?.let { state ->
        var tempColor by remember { mutableStateOf(HsvColor.from(state.initialColor)) }

        AlertDialog(
            onDismissRequest = { colorPickerState = null },
            title = { Text("Выберите цвет для \"${state.label}\"") },
            text = {
                ClassicColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
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
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { colorPickerState = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ColorSettingRow(
    label: String,
    color: Color,
    onClick: (Color) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(color) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                String.format("#%08X", color.toArgb()),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color)
            )
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

            // сетка
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
            drawRect(
                Color.Black.copy(alpha = 0.4f),
                topLeft = bedRect.topLeft,
                size = bedRect.size,
                style = Stroke(1f)
            )

            // теплица
            val greenhouseRect = Rect(
                worldToScreen(55f, 10f),
                worldToScreen(90f, 30f)
            )
            drawRect(greenhouse, topLeft = greenhouseRect.topLeft, size = greenhouseRect.size)
            drawRect(
                Color.Black.copy(alpha = 0.4f),
                topLeft = greenhouseRect.topLeft,
                size = greenhouseRect.size,
                style = Stroke(1f)
            )

            // здание
            val buildingRect = Rect(
                worldToScreen(20f, 45f),
                worldToScreen(55f, 75f)
            )
            drawRect(building, topLeft = buildingRect.topLeft, size = buildingRect.size)
            drawRect(
                Color.Black.copy(alpha = 0.6f),
                topLeft = buildingRect.topLeft,
                size = buildingRect.size,
                style = Stroke(1.5f)
            )

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
                if (index == 1) {
                    drawCircle(
                        color = selectedStroke,
                        radius = radius + w(1f),
                        center = center,
                        style = Stroke(width = w(0.5f))
                    )
                }
            }

            // подписи
            val bedLabel = textMeasurer.measure(
                text = "Грядка",
                style = TextStyle(fontSize = 10.sp, color = text)
            )
            drawText(
                bedLabel,
                topLeft = bedRect.center - Offset(
                    bedLabel.size.width / 2f,
                    bedLabel.size.height / 2f
                )
            )

            val greenhouseLabel = textMeasurer.measure(
                text = "Теплица",
                style = TextStyle(fontSize = 10.sp, color = text)
            )
            drawText(
                greenhouseLabel,
                topLeft = greenhouseRect.center - Offset(
                    greenhouseLabel.size.width / 2f,
                    greenhouseLabel.size.height / 2f
                )
            )

            val buildingLabel = textMeasurer.measure(
                text = "Дом",
                style = TextStyle(fontSize = 10.sp, color = text)
            )
            drawText(
                buildingLabel,
                topLeft = buildingRect.center - Offset(
                    buildingLabel.size.width / 2f,
                    buildingLabel.size.height / 2f
                )
            )
        }
    }
}
