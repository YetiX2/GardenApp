package com.example.gardenapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.gardenapp.data.settings.SettingsManager
import com.example.gardenapp.data.settings.ThemeOption
import com.example.gardenapp.nav.AppNav
import com.example.gardenapp.ui.theme.GardenAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        installSplashScreen()
        enableEdgeToEdge()
        
        setContent {
            val theme by settingsManager.themeFlow.collectAsState(initial = ThemeOption.SYSTEM)
            GardenAppTheme(themeOption = theme) {
                AppNav()
            }
        }
    }
}
