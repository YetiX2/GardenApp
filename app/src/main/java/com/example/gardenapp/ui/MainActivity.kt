package com.example.gardenapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.gardenapp.data.settings.SettingsManager
import com.example.gardenapp.data.settings.ThemeOption
import com.example.gardenapp.nav.AppNav
import com.example.gardenapp.ui.onboarding.OnboardingScreen
import com.example.gardenapp.ui.theme.GardenAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
            val scope = rememberCoroutineScope()
            val theme by settingsManager.themeFlow.collectAsState(initial = ThemeOption.SYSTEM)
            val hasSeenOnboarding by settingsManager.hasSeenOnboarding.collectAsState(initial = false)
            
            GardenAppTheme(themeOption = theme) {
                if (hasSeenOnboarding) {
                    AppNav()
                } else {
                    OnboardingScreen(
                        onFinished = {
                            scope.launch {
                                settingsManager.setOnboardingSeen()
                            }
                        }
                    )
                }
            }
        }
    }
}
