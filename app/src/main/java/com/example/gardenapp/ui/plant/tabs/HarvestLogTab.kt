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
import com.example.gardenapp.data.db.HarvestLogEntity
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HarvestLogTab(logs: List<HarvestLogEntity>, onAdd: () -> Unit, onDelete: (HarvestLogEntity) -> Unit) {
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
                        val formattedWeight = String.format(Locale.getDefault(), "%.1f", log.weightKg)
                        ListItem(
                            headlineContent = { Text("$formattedWeight кг - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
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
