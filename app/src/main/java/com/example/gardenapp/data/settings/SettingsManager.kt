package com.example.gardenapp.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeOption { LIGHT, DARK, SYSTEM }

@Singleton
class SettingsManager @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        // Theme
        val THEME = stringPreferencesKey("theme_option")
        
        // Onboarding
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        
        // Colors
        val plantColor = intPreferencesKey("plant_color")
        val bedColor = intPreferencesKey("bed_color")
        val greenhouseColor = intPreferencesKey("greenhouse_color")
        val buildingColor = intPreferencesKey("building_color")
        val gridColor = intPreferencesKey("grid_color")
        val gardenBackgroundColor = intPreferencesKey("garden_background_color")
        val textColor = intPreferencesKey("text_color")
        val selectedStrokeColor = intPreferencesKey("selected_stroke_color")
    }

    // Theme Flow
    val themeFlow = dataStore.data.map { it[Keys.THEME]?.let { ThemeOption.valueOf(it) } ?: ThemeOption.SYSTEM }

    suspend fun setTheme(theme: ThemeOption) = dataStore.edit { it[Keys.THEME] = theme.name }

    // Onboarding Flow
    val hasSeenOnboarding = dataStore.data.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setOnboardingSeen() = dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = true }

    // Color Flows
    val plantColor = dataStore.data.map { it[Keys.plantColor] }
    val bedColor = dataStore.data.map { it[Keys.bedColor] }
    val greenhouseColor = dataStore.data.map { it[Keys.greenhouseColor] }
    val buildingColor = dataStore.data.map { it[Keys.buildingColor] }
    val gridColor = dataStore.data.map { it[Keys.gridColor] }
    val gardenBackgroundColor = dataStore.data.map { it[Keys.gardenBackgroundColor] }
    val textColor = dataStore.data.map { it[Keys.textColor] }
    val selectedStrokeColor = dataStore.data.map { it[Keys.selectedStrokeColor] }

    // Color Save Methods
    suspend fun savePlantColor(color: Int) = dataStore.edit { it[Keys.plantColor] = color }
    suspend fun saveBedColor(color: Int) = dataStore.edit { it[Keys.bedColor] = color }
    suspend fun saveGreenhouseColor(color: Int) = dataStore.edit { it[Keys.greenhouseColor] = color }
    suspend fun saveBuildingColor(color: Int) = dataStore.edit { it[Keys.buildingColor] = color }
    suspend fun saveGridColor(color: Int) = dataStore.edit { it[Keys.gridColor] = color }
    suspend fun saveGardenBackgroundColor(color: Int) = dataStore.edit { it[Keys.gardenBackgroundColor] = color }
    suspend fun saveTextColor(color: Int) = dataStore.edit { it[Keys.textColor] = color }
    suspend fun saveSelectedStrokeColor(color: Int) = dataStore.edit { it[Keys.selectedStrokeColor] = color }

    suspend fun clearAllColors() {
        dataStore.edit {
            it.remove(Keys.plantColor)
            it.remove(Keys.bedColor)
            it.remove(Keys.greenhouseColor)
            it.remove(Keys.buildingColor)
            it.remove(Keys.gridColor)
            it.remove(Keys.gardenBackgroundColor)
            it.remove(Keys.textColor)
            it.remove(Keys.selectedStrokeColor)
        }
    }
}
