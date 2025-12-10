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
import com.example.gardenapp.data.db.FertilizerLogEntity
import java.time.format.DateTimeFormatter

@Composable
fun FertilizerLogTab(logs: List<FertilizerLogEntity>, onAdd: () -> Unit, onDelete: (FertilizerLogEntity) -> Unit) {
    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, null) } }
    ) {
        padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
            if (logs.isEmpty()) item { Text("Записей об удобрении пока нет.") }
            items(logs, key = { it.id }) { log ->
                ListItem(
                    headlineContent = { Text("${log.amountGrams}г - ${log.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}") },
                    supportingContent = { log.note?.let { Text(it) } },
                    trailingContent = { IconButton(onClick = { onDelete(log) }) { Icon(Icons.Default.Delete, null) } }
                )
            }
        }
    }
}
