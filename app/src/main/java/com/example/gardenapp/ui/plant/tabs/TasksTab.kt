package com.example.gardenapp.ui.plant.tabs

import androidx.compose.runtime.Composable
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskWithPlantInfo
import com.example.gardenapp.ui.common.TaskList

@Composable
fun TasksTab(
    tasks: List<TaskWithPlantInfo>,
    onStatusChange: (String, TaskStatus) -> Unit
) {
    TaskList(tasks = tasks, onStatusChange = onStatusChange)
}
