package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.FertilizerLogEntity
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FertilizerLogTab(logs: List<FertilizerLogEntity>, onAdd: () -> Unit, onDelete: (FertilizerLogEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Recommendations Placeholder ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Рекомендации по подкормке") },
                    supportingContent = { Text("Здесь будут общие рекомендации по удобрениям, подходящим для этого вида растений.") }
                )
            }

            Text("Журнал подкормок", style = MaterialTheme.typography.titleMedium)

            // --- Log List ---
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (logs.isEmpty()) {
                    item { Text("Записей о подкормках пока нет.") }
                } else {
                    items(logs, key = { it.id }) { log ->
                        val formattedAmount = String.format(Locale.getDefault(), "%.1f", log.amountGrams)
                        ListItem(
                            headlineContent = { Text("$formattedAmount г - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                            supportingContent = { log.note?.let { Text(it) } },
                            trailingContent = { IconButton(onClick = { onDelete(log) }) { Icon(Icons.Default.Delete, null) } }
                        )
                    }
                }
            }

            // --- Ad Block ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Лучшие удобрения") },
                    supportingContent = { Text("в аффилированном магазине") },
                    trailingContent = { Icon(Icons.Outlined.Link, null) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer for padding at the bottom
        }
    }
}
