package com.example.gardenapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gardenapp.data.settings.ThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by vm.themeOption.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Theme Section
            Text("Тема оформления", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ThemeSwitcher(selectedTheme = currentTheme, onThemeChange = { vm.setTheme(it) })

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

            // Language Section (Placeholder)
            Text("Язык", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Выбор языка", color = disabledColor) },
                supportingContent = { Text("В разработке", color = disabledColor) }//TODO
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))

            // Database Section (Placeholders)
            Text("База данных", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                headlineContent = { Text("Экспорт базы данных", color = disabledColor) }//TODO
            )
            ListItem(
                headlineContent = { Text("Импорт базы данных", color = disabledColor) }//TODO
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSwitcher(
    selectedTheme: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit
) {
    val options = listOf(
        ThemeOption.LIGHT to Icons.Default.LightMode,
        ThemeOption.DARK to Icons.Default.DarkMode,
        ThemeOption.SYSTEM to Icons.Default.Sync
    )

    MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (theme, icon) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = { onThemeChange(theme) },
                checked = theme == selectedTheme,
                icon = { 
                    Icon(icon, contentDescription = theme.name, modifier = Modifier.width(24.dp)) 
                }
            ) {
                Text(theme.name.lowercase().replaceFirstChar { it.titlecase() })
            }
        }
    }
}
