package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.HarvestLogEntity
import java.time.format.DateTimeFormatter

@Composable
fun HarvestLogTab(logs: List<HarvestLogEntity>, onAdd: () -> Unit, onDelete: (HarvestLogEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) {
        padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Recommendations Placeholder ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Сбор и хранение урожая") },
                    supportingContent = { Text("Здесь будет информация о датах созревания, сборе и правилах хранения плодов.") }
                )
            }
            
            Text("Журнал сбора урожая", style = MaterialTheme.typography.titleMedium)

            // --- Log List ---
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (logs.isEmpty()) {
                    item { Text("Записей об урожае пока нет.") }
                } else {
                    items(logs, key = { it.id }) { log ->
                        ListItem(
                            headlineContent = { Text("${log.weightKg}кг - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                            supportingContent = { log.note?.let { Text(it) } },
                            trailingContent = { IconButton(onClick = { onDelete(log) }) { Icon(Icons.Default.Delete, null) } }
                        )
                    }
                }
            }
            
            // --- Ad Block ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Товары для хранения урожая") },
                    supportingContent = { Text("в аффилированном магазине") },
                    trailingContent = { Icon(Icons.Outlined.Link, null) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer for padding at the bottom
        }
    }
}
