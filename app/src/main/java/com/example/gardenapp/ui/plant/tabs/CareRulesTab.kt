package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.CareRuleEntity
import com.example.gardenapp.data.db.TaskType
import java.time.format.DateTimeFormatter

private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.OTHER -> "Другое"
}

@Composable
fun CareRulesTab(rules: List<CareRuleEntity>, onAdd: () -> Unit, onDelete: (CareRuleEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) {
            padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            if (rules.isEmpty()) item { Text("Правил ухода пока нет.") }
            items(rules, key = { it.id }) { rule ->
                val everyText = rule.everyDays?.let { "каждые $it дней" } ?: ""
                ListItem(
                    headlineContent = { Text("${rule.type.toRussian()} $everyText") },
                    supportingContent = { Text("Начиная с ${rule.start.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                    trailingContent = { IconButton(onClick = { onDelete(rule) }) { Icon(Icons.Default.Delete, null) } }
                )
            }
        }
    }
}
