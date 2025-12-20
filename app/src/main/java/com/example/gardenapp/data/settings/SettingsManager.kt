package com.example.gardenapp.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeOption { LIGHT, DARK, SYSTEM }

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme_option")
        val hasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
        val dataVersion = intPreferencesKey("data_version") // ADDED
    }

    val themeFlow = context.dataStore.data.map { preferences ->
        val themeName = preferences[Keys.theme] ?: ThemeOption.SYSTEM.name
        ThemeOption.valueOf(themeName)
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.hasSeenOnboarding] ?: false
    }

    val dataVersion: Flow<Int> = context.dataStore.data.map { // ADDED
        it[Keys.dataVersion] ?: 0
    }

    suspend fun setTheme(theme: ThemeOption) {
        context.dataStore.edit { it[Keys.theme] = theme.name }
    }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { it[Keys.hasSeenOnboarding] = true }
    }

    suspend fun setDataVersion(version: Int) { // ADDED
        context.dataStore.edit { it[Keys.dataVersion] = version }
    }
}
