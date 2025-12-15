package com.example.gardenapp.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "colorSettings")

@Singleton
class ColorSettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val plantColor = intPreferencesKey("plant_color")
        val bedColor = intPreferencesKey("bed_color")
        val greenhouseColor = intPreferencesKey("greenhouse_color")
        val buildingColor = intPreferencesKey("building_color")
        val gridColor = intPreferencesKey("grid_color") // ADDED
        val gardenBackgroundColor = intPreferencesKey("garden_background_color") // ADDED

        val textColor = intPreferencesKey("text_color") // ADDED
        val selectedStrokeColor = intPreferencesKey("garden_selected_stroke_color") // ADDED
    }

    val plantColor = context.dataStore.data.map { it[Keys.plantColor] }
    val bedColor = context.dataStore.data.map { it[Keys.bedColor] }
    val greenhouseColor = context.dataStore.data.map { it[Keys.greenhouseColor] }
    val buildingColor = context.dataStore.data.map { it[Keys.buildingColor] }
    val gridColor = context.dataStore.data.map { it[Keys.gridColor] } // ADDED
    val gardenBackgroundColor = context.dataStore.data.map { it[Keys.gardenBackgroundColor] } // ADDED

    val textColor = context.dataStore.data.map { it[Keys.textColor] } // ADDED
    val selectedStrokeColor = context.dataStore.data.map { it[Keys.selectedStrokeColor] } // ADDED

    suspend fun savePlantColor(color: Int) {
        context.dataStore.edit { it[Keys.plantColor] = color }
    }

    suspend fun saveBedColor(color: Int) {
        context.dataStore.edit { it[Keys.bedColor] = color }
    }

    suspend fun saveGreenhouseColor(color: Int) {
        context.dataStore.edit { it[Keys.greenhouseColor] = color }
    }

    suspend fun saveBuildingColor(color: Int) {
        context.dataStore.edit { it[Keys.buildingColor] = color }
    }

    suspend fun saveGridColor(color: Int) { // ADDED
        context.dataStore.edit { it[Keys.gridColor] = color }
    }

    suspend fun saveGardenBackgroundColor(color: Int) { // ADDED
        context.dataStore.edit { it[Keys.gardenBackgroundColor] = color }
    }

    suspend fun saveTextColor(color: Int) { // ADDED
        context.dataStore.edit { it[Keys.textColor] = color }
    }

    suspend fun saveSelectedStrokeColor(color: Int) { // ADDED
        context.dataStore.edit { it[Keys.selectedStrokeColor] = color }
    }

}
