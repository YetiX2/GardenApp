package com.example.gardenapp.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "colorSettings")

@Singleton
class ColorSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        // ---- LIGHT ----
        val plantColor = intPreferencesKey("plant_color")
        val bedColor = intPreferencesKey("bed_color")
        val greenhouseColor = intPreferencesKey("greenhouse_color")
        val buildingColor = intPreferencesKey("building_color")
        val gridColor = intPreferencesKey("grid_color")
        val gardenBackgroundColor = intPreferencesKey("garden_background_color")
        val textColor = intPreferencesKey("text_color")
        val selectedStrokeColor = intPreferencesKey("garden_selected_stroke_color")

        // ---- DARK ----
        val plantColorDark = intPreferencesKey("plant_color_dark")
        val bedColorDark = intPreferencesKey("bed_color_dark")
        val greenhouseColorDark = intPreferencesKey("greenhouse_color_dark")
        val buildingColorDark = intPreferencesKey("building_color_dark")
        val gridColorDark = intPreferencesKey("grid_color_dark")
        val gardenBackgroundColorDark = intPreferencesKey("garden_background_color_dark")
        val textColorDark = intPreferencesKey("text_color_dark")
        val selectedStrokeColorDark = intPreferencesKey("garden_selected_stroke_color_dark")

        // ---- FLAG ----
        val useSeparateDarkPalette = booleanPreferencesKey("use_separate_dark_palette")
    }

    // --------- LIGHT ---------
    val plantColor: Flow<Int?> = context.dataStore.data.map { it[Keys.plantColor] }
    val bedColor: Flow<Int?> = context.dataStore.data.map { it[Keys.bedColor] }
    val greenhouseColor: Flow<Int?> = context.dataStore.data.map { it[Keys.greenhouseColor] }
    val buildingColor: Flow<Int?> = context.dataStore.data.map { it[Keys.buildingColor] }
    val gridColor: Flow<Int?> = context.dataStore.data.map { it[Keys.gridColor] }
    val gardenBackgroundColor: Flow<Int?> = context.dataStore.data.map { it[Keys.gardenBackgroundColor] }
    val textColor: Flow<Int?> = context.dataStore.data.map { it[Keys.textColor] }
    val selectedStrokeColor: Flow<Int?> = context.dataStore.data.map { it[Keys.selectedStrokeColor] }

    suspend fun savePlantColor(color: Int) = context.dataStore.edit { it[Keys.plantColor] = color }
    suspend fun saveBedColor(color: Int) = context.dataStore.edit { it[Keys.bedColor] = color }
    suspend fun saveGreenhouseColor(color: Int) = context.dataStore.edit { it[Keys.greenhouseColor] = color }
    suspend fun saveBuildingColor(color: Int) = context.dataStore.edit { it[Keys.buildingColor] = color }
    suspend fun saveGridColor(color: Int) = context.dataStore.edit { it[Keys.gridColor] = color }
    suspend fun saveGardenBackgroundColor(color: Int) = context.dataStore.edit { it[Keys.gardenBackgroundColor] = color }
    suspend fun saveTextColor(color: Int) = context.dataStore.edit { it[Keys.textColor] = color }
    suspend fun saveSelectedStrokeColor(color: Int) = context.dataStore.edit { it[Keys.selectedStrokeColor] = color }

    // --------- DARK ---------
    val plantColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.plantColorDark] }
    val bedColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.bedColorDark] }
    val greenhouseColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.greenhouseColorDark] }
    val buildingColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.buildingColorDark] }
    val gridColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.gridColorDark] }
    val gardenBackgroundColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.gardenBackgroundColorDark] }
    val textColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.textColorDark] }
    val selectedStrokeColorDark: Flow<Int?> = context.dataStore.data.map { it[Keys.selectedStrokeColorDark] }

    suspend fun savePlantColorDark(color: Int) = context.dataStore.edit { it[Keys.plantColorDark] = color }
    suspend fun saveBedColorDark(color: Int) = context.dataStore.edit { it[Keys.bedColorDark] = color }
    suspend fun saveGreenhouseColorDark(color: Int) = context.dataStore.edit { it[Keys.greenhouseColorDark] = color }
    suspend fun saveBuildingColorDark(color: Int) = context.dataStore.edit { it[Keys.buildingColorDark] = color }
    suspend fun saveGridColorDark(color: Int) = context.dataStore.edit { it[Keys.gridColorDark] = color }
    suspend fun saveGardenBackgroundColorDark(color: Int) = context.dataStore.edit { it[Keys.gardenBackgroundColorDark] = color }
    suspend fun saveTextColorDark(color: Int) = context.dataStore.edit { it[Keys.textColorDark] = color }
    suspend fun saveSelectedStrokeColorDark(color: Int) = context.dataStore.edit { it[Keys.selectedStrokeColorDark] = color }

    // --------- FLAG ---------
    // ВАЖНО: дефолт = TRUE (переключатель включён по умолчанию)
    val useSeparateDarkPalette: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.useSeparateDarkPalette] ?: true
    }

    suspend fun setUseSeparateDarkPalette(value: Boolean) {
        context.dataStore.edit { it[Keys.useSeparateDarkPalette] = value }
    }

    // --------- RESET ---------
    suspend fun clearAllColors() {
        context.dataStore.edit {
            // light
            it.remove(Keys.plantColor)
            it.remove(Keys.bedColor)
            it.remove(Keys.greenhouseColor)
            it.remove(Keys.buildingColor)
            it.remove(Keys.gridColor)
            it.remove(Keys.gardenBackgroundColor)
            it.remove(Keys.textColor)
            it.remove(Keys.selectedStrokeColor)

            // dark
            it.remove(Keys.plantColorDark)
            it.remove(Keys.bedColorDark)
            it.remove(Keys.greenhouseColorDark)
            it.remove(Keys.buildingColorDark)
            it.remove(Keys.gridColorDark)
            it.remove(Keys.gardenBackgroundColorDark)
            it.remove(Keys.textColorDark)
            it.remove(Keys.selectedStrokeColorDark)

            // flag
            it.remove(Keys.useSeparateDarkPalette)
        }
    }
}
