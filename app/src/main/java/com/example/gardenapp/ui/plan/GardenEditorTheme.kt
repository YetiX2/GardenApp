// GardenEditorTheme.kt
package com.example.gardenapp.ui.plan

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.ui.settings.ColorSettingsVm

data class GardenEditorColors(
    val background: Color,
    val grid: Color,
    val bed: Color,
    val greenhouse: Color,
    val building: Color,
    val plant: Color,
    val selectedStroke: Color,
    val text: Color,
)

// базовые палитры (мои предложенные цвета)

private object GardenEditorPalettes {

    val LightBase = GardenEditorColors(
        background     = Color(0xFFF2F0EB),                         // тёплый беж (фон участка)
        grid           = Color(0xFF9E9E9E).copy(alpha = 0.20f),      // лёгкая серая сетка
        bed            = Color(0xFF4CAF50).copy(alpha = 0.22f),      // грядки
        greenhouse     = Color(0xFF009688).copy(alpha = 0.22f),      // теплица
        building       = Color(0xFF8D6E63).copy(alpha = 0.60f),      // здания
        plant          = Color(0xFF2E7D32),                          // растения
        selectedStroke = Color(0xFFFF9800),                          // выделение
        text           = Color(0xFF333333),                          // подписи
    )

    val DarkBase = GardenEditorColors(
        background     = Color(0xFF1B1B1B),
        grid           = Color(0xFFFFFFFF).copy(alpha = 0.06f),
        bed            = Color(0xFF66BB6A).copy(alpha = 0.35f),
        greenhouse     = Color(0xFF80CBC4).copy(alpha = 0.35f),
        building       = Color(0xFFBCAAA4).copy(alpha = 0.70f),
        plant          = Color(0xFFA5D6A7),
        selectedStroke = Color(0xFFFFB74D),
        text           = Color(0xFFF5F5F5),
    )
}

val LocalGardenEditorColors = staticCompositionLocalOf {
    // дефолт, если что-то пошло не так
    GardenEditorPalettes.LightBase
}

object GardenEditorTheme {
    val colors: GardenEditorColors
        @Composable get() = LocalGardenEditorColors.current
}

/**
 * Тема редактора, учитывающая:
 * - текущую светлую/тёмную тему (darkTheme)
 * - кастомные цвета из ColorSettingsVm (если пользователь их менял)
 */
@Composable
fun GardenEditorThemeFromSettings(
    darkTheme: Boolean, // <- важно: без default isSystemInDarkTheme()
    vm: ColorSettingsVm = hiltViewModel(),
    content: @Composable () -> Unit
) {
    // Базовая палитра по фактической теме
    val base = if (darkTheme) GardenEditorPalettes.DarkBase else GardenEditorPalettes.LightBase

    // Флаг: раздельные палитры для светлой/тёмной темы
    val useSeparateDark = vm.useSeparateDarkPalette.collectAsState(initial = false).value

    // Если флаг включен и сейчас darkTheme — берём DARK ключи; иначе LIGHT ключи
    val useDarkKeys = useSeparateDark && darkTheme

    val plantColorInt = (if (useDarkKeys) vm.settings.plantColorDark else vm.settings.plantColor)
        .collectAsState(initial = null).value
    val bedColorInt = (if (useDarkKeys) vm.settings.bedColorDark else vm.settings.bedColor)
        .collectAsState(initial = null).value
    val greenhouseColorInt = (if (useDarkKeys) vm.settings.greenhouseColorDark else vm.settings.greenhouseColor)
        .collectAsState(initial = null).value
    val buildingColorInt = (if (useDarkKeys) vm.settings.buildingColorDark else vm.settings.buildingColor)
        .collectAsState(initial = null).value
    val gridColorInt = (if (useDarkKeys) vm.settings.gridColorDark else vm.settings.gridColor)
        .collectAsState(initial = null).value
    val backgroundColorInt = (if (useDarkKeys) vm.settings.gardenBackgroundColorDark else vm.settings.gardenBackgroundColor)
        .collectAsState(initial = null).value
    val textColorInt = (if (useDarkKeys) vm.settings.textColorDark else vm.settings.textColor)
        .collectAsState(initial = null).value
    val selectedStrokeColorInt = (if (useDarkKeys) vm.settings.selectedStrokeColorDark else vm.settings.selectedStrokeColor)
        .collectAsState(initial = null).value

    val editorColors = GardenEditorColors(
        background     = backgroundColorInt?.let(::Color) ?: base.background,
        grid           = gridColorInt?.let(::Color) ?: base.grid,
        bed            = bedColorInt?.let(::Color) ?: base.bed,
        greenhouse     = greenhouseColorInt?.let(::Color) ?: base.greenhouse,
        building       = buildingColorInt?.let(::Color) ?: base.building,
        plant          = plantColorInt?.let(::Color) ?: base.plant,
        selectedStroke = selectedStrokeColorInt?.let(::Color) ?: base.selectedStroke,
        text           = textColorInt?.let(::Color) ?: base.text,
    )

    CompositionLocalProvider(LocalGardenEditorColors provides editorColors) {
        content()
    }
}
