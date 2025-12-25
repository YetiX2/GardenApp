package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.TaskWithPlantInfo
import com.example.gardenapp.data.db.toRussian
import com.example.gardenapp.ui.common.TaskSummary
import java.time.format.DateTimeFormatter

@Composable
fun TodayTasksCard(tasks: List<TaskWithPlantInfo>, onPlantClick: (String) -> Unit, onOpenTasks: () -> Unit) {
    Card(onClick = onOpenTasks) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Сегодняшние задачи", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            TaskSummary(tasks = tasks)
        }
    }
}
