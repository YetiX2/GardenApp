package com.example.gardenapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.gardenapp.data.settings.SettingsManager
import com.example.gardenapp.data.settings.ThemeOption
import com.example.gardenapp.nav.AppNav
import com.example.gardenapp.ui.onboarding.OnboardingScreen
import com.example.gardenapp.ui.theme.GardenAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data object ShowOnboarding : MainActivityUiState
    data object ShowApp : MainActivityUiState
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // MOVED TO THE TOP

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        installSplashScreen().setKeepOnScreenCondition {
            uiState == MainActivityUiState.Loading
        }

        // Now that super.onCreate() has been called, Hilt has injected settingsManager.
        // It's safe to access it here.
        lifecycleScope.launch {
            val hasSeenOnboarding = settingsManager.hasSeenOnboarding.first()
            uiState = if (hasSeenOnboarding) {
                MainActivityUiState.ShowApp
            } else {
                MainActivityUiState.ShowOnboarding
            }
        }
        
        enableEdgeToEdge()
        
        setContent {
            val scope = rememberCoroutineScope()
            val theme by settingsManager.themeFlow.collectAsState(initial = ThemeOption.SYSTEM)
            
            GardenAppTheme(themeOption = theme) {
                when (uiState) {
                    MainActivityUiState.Loading -> { /* Keep splash screen on */ }
                    MainActivityUiState.ShowApp -> { AppNav() }
                    MainActivityUiState.ShowOnboarding -> {
                        OnboardingScreen(
                            onFinished = {
                                scope.launch { settingsManager.setOnboardingSeen() }
                                uiState = MainActivityUiState.ShowApp 
                            }
                        )
                    }
                }
            }
        }
    }
}
