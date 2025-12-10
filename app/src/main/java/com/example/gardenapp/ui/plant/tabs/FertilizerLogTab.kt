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
import com.example.gardenapp.data.db.FertilizerLogEntity
import java.time.format.DateTimeFormatter

@Composable
fun FertilizerLogTab(logs: List<FertilizerLogEntity>, onAdd: () -> Unit, onDelete: (FertilizerLogEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) {
        padding ->
        // Main column to hold recommendations, list, and ad
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Recommendations Placeholder ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Рекомендации по внесению удобрений") },
                    supportingContent = { Text("Здесь будет информация о сроках и объемах внесения удобрений для этого растения.") }
                )
            }
            
            Text("Журнал внесения удобрений", style = MaterialTheme.typography.titleMedium)

            // --- Log List ---
            if (logs.isEmpty()) {
                Text("Записей об удобрении пока нет.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs, key = { it.id }) {
                        log ->
                        ListItem(
                            headlineContent = { Text("${log.amountGrams}г - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                            supportingContent = { log.note?.let { Text(it) } },
                            trailingContent = { IconButton(onClick = { onDelete(log) }) { Icon(Icons.Default.Delete, null) } }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // --- Ad Block ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Купить удобрения") },
                    supportingContent = { Text("на Ozon / Wildberries") },
                    trailingContent = { Icon(Icons.Outlined.Link, null) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer for padding at the bottom
        }
    }
}
