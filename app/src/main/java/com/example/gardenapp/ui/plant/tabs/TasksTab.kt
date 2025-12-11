package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gardenapp.data.db.TaskStatus
import com.example.gardenapp.data.db.TaskWithPlantInfo
import com.example.gardenapp.ui.common.TaskList

@Composable
fun TasksTab(
    tasks: List<TaskWithPlantInfo>,
    onStatusChange: (String, TaskStatus) -> Unit,
    onAdd: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Добавить задачу")
            }
        }
    ) { padding ->
        TaskList(tasks = tasks, onStatusChange = onStatusChange, modifier = Modifier.padding(padding))
    }
}
