package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskWithPlantInfo
import com.example.gardenapp.ui.common.TaskSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksCard(tasks: List<TaskWithPlantInfo>, onOpenTasks: () -> Unit) {
    Card(onClick = onOpenTasks) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Сегодняшние задачи", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            TaskSummary(tasks = tasks)
        }
    }
}
