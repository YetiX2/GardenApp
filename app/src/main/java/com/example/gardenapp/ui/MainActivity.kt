package com.example.gardenapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gardenapp.ui.nav.AppNav
import dagger.hilt.android.AndroidEntryPoint
import com.example.gardenapp.ui.plan.GardenPlanScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // В демо — пока один фиксированный gardenId (создайте сад через другой экран позже)
                    // Для теста можно временно задать какой-то ID, UI позволит добавлять растения.
                    GardenPlanScreen(gardenId = "demo-garden-id")
                }
            }
        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme { Surface(Modifier.fillMaxSize()) { AppNav() } }
        }
    }
}
