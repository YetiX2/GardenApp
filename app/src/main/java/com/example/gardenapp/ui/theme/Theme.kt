package com.example.gardenapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.gardenapp.data.settings.ThemeOption

/**
 * Источник истины для "эффективной" темы (dark/light) с учётом ThemeOption.SYSTEM.
 * Используй LocalIsDarkTheme.current в экранах (например, чтобы выбрать палитру Canvas).
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun GardenAppTheme(
    themeOption: ThemeOption = ThemeOption.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = when (themeOption) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalIsDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
