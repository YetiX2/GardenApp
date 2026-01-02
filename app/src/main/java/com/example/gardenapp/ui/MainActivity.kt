package com.example.gardenapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining to the user the features that will be enabled
                //       by granting the POST_NOTIFICATION permission. This UI should provide the user
                //       with two choices: one to grant the permission and one to dismiss the request.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        installSplashScreen().setKeepOnScreenCondition {
            uiState == MainActivityUiState.Loading
        }

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
                    MainActivityUiState.Loading -> { /* Keep splash screen on */
                    }

                    MainActivityUiState.ShowApp -> {
                        AppNav()
                    }

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
        askNotificationPermission()
    }
}
