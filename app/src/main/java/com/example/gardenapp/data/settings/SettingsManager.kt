package com.example.gardenapp.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
        val THEME = stringPreferencesKey("theme_option")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding") // ADDED
    }

    val themeFlow = context.dataStore.data.map { preferences ->
        val themeName = preferences[Keys.THEME] ?: ThemeOption.SYSTEM.name
        ThemeOption.valueOf(themeName)
    }

    suspend fun setTheme(theme: ThemeOption) {
        context.dataStore.edit {
                settings -> settings[Keys.THEME] = theme.name
        }
    }

    // ADDED
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map {
        it[Keys.HAS_SEEN_ONBOARDING] ?: false
    }

    // ADDED
    suspend fun setOnboardingSeen() {
        context.dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = true }
    }
}
