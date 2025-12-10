package com.example.gardenapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gardenapp.ui.dashboard.DashboardScreen
import com.example.gardenapp.ui.gardenlist.GardenListScreen
import com.example.gardenapp.ui.plan.GardenPlanScreen
import com.example.gardenapp.ui.plant.PlantEditorScreen
import com.example.gardenapp.ui.tasks.TaskListScreen

sealed class Route(val value: String) {
    data object Dashboard : Route("dashboard")
    data object Gardens : Route("gardens")
    data object Plan : Route("plan/{gardenId}")
    data object PlantEditor : Route("plant/{plantId}") // Added route for Plant Editor
    data object Tasks : Route("tasks")
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Dashboard.value) {
        composable(Route.Dashboard.value) {
            DashboardScreen(
                onOpenGardens = { nav.navigate(Route.Gardens.value) },
                onOpenTasks = { nav.navigate(Route.Tasks.value) }
            )
        }
        composable(Route.Gardens.value) {
            GardenListScreen(
                onOpen = { id -> nav.navigate("plan/$id") },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Route.Plan.value) { backStack ->
            val id = backStack.arguments?.getString("gardenId")!!
            GardenPlanScreen(
                gardenId = id, 
                onBack = { nav.popBackStack() },
                onOpenPlant = { plantId -> nav.navigate("plant/$plantId") } // Connect to Plant Editor
            )
        }
        composable(Route.PlantEditor.value) { backStack ->
            // ViewModel will be created automatically by Hilt and will receive plantId via SavedStateHandle
            PlantEditorScreen(onBack = { nav.popBackStack() })
        }
        composable(Route.Tasks.value) {
            TaskListScreen(onBack = { nav.popBackStack() })
        }
    }
}
