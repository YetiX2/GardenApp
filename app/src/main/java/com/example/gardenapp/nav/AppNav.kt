package com.example.gardenapp.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gardenapp.ui.dashboard.DashboardScreen
import com.example.gardenapp.ui.dashboard.SeasonStatsScreen
import com.example.gardenapp.ui.gardenlist.GardenListScreen
import com.example.gardenapp.ui.plant.PlantEditorScreen
import com.example.gardenapp.ui.plan.GardenPlanScreen
import com.example.gardenapp.ui.settings.ColorSettingsScreen
import com.example.gardenapp.ui.settings.SettingsScreen
import com.example.gardenapp.ui.tasks.TaskListScreen

sealed class Route(val value: String) {
    data object Dashboard : Route("dashboard")
    data object Gardens : Route("gardens")
    data object Plan : Route("plan/{gardenId}")
    data object PlantEditor : Route("plant/{plantId}")
    data object Tasks : Route("tasks")
    data object Settings : Route("settings")
    data object ColorSettings : Route("colorSettings")
    data object SeasonStats : Route("seasonStats")
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Dashboard.value) {
        composable(Route.Dashboard.value) {
            DashboardScreen(
                onOpenGardens = { nav.navigate(Route.Gardens.value) },
                onOpenTasks = { nav.navigate(Route.Tasks.value) },
                onOpenSettings = { nav.navigate(Route.Settings.value) },
                onOpenPlant = { plantId -> nav.navigate("plant/$plantId") },
                onSeasonStatsClick = { nav.navigate(Route.SeasonStats.value) }
            )
        }
        composable(Route.Gardens.value) {
            GardenListScreen(
                onOpen = { id -> nav.navigate("plan/$id") },
                onBack = { nav.popBackStack() },
                onOpenSettings = { nav.navigate(Route.ColorSettings.value) }
            )
        }
        composable(Route.Plan.value) { backStack ->
            val id = backStack.arguments?.getString("gardenId")!!
            GardenPlanScreen(
                gardenId = id, 
                onBack = { nav.popBackStack() },
                onOpenPlant = { plantId -> nav.navigate("plant/$plantId") },
                onOpenGarden = { gardenId -> nav.navigate("plan/$gardenId") }
            )
        }
        composable(Route.PlantEditor.value) { backStack ->
            PlantEditorScreen(onBack = { nav.popBackStack() })
        }
        composable(Route.Tasks.value) {
            TaskListScreen(onBack = { nav.popBackStack() })
        }
        composable(Route.Settings.value) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Route.ColorSettings.value) {
            ColorSettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Route.SeasonStats.value) { 
            SeasonStatsScreen(
                onNavigateBack = { nav.popBackStack() },
                onOpenGarden = { gardenId -> nav.navigate("plan/$gardenId") },
                onOpenPlant = { plantId -> nav.navigate("plant/$plantId") }
            )
        }
    }
}
