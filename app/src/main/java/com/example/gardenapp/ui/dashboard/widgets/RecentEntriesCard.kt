package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.RecentActivity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentEntriesCard(
    activityItems: List<RecentActivity>,
    onOpenPlant: (String) -> Unit // ADDED
) {
    Column {
        Text("Последние записи", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (activityItems.isEmpty()) {
            Text("Пока нет недавних записей.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                activityItems.forEach { item ->
                    val plantId = when (item) { // Get plantId from the item
                        is RecentActivity.Fertilizer -> item.data.log.plantId
                        is RecentActivity.Harvest -> item.data.log.plantId
                    }
                    Card(
                        onClick = { onOpenPlant(plantId) }, // ADDED
                        modifier = Modifier.weight(1f)
                    ) {
                        when (item) {
                            is RecentActivity.Fertilizer -> {
                                val formattedAmount = String.format(Locale.getDefault(), "%.1f", item.data.log.amountGrams)
                                val text = "Удобрение: ${formattedAmount}г для \"${item.data.plantName}\""
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(8.dp))
                                    Text(text, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            is RecentActivity.Harvest -> {
                                val formattedWeight = String.format(Locale.getDefault(), "%.1f", item.data.log.weightKg)
                                val text = "Урожай: ${formattedWeight}кг с \"${item.data.plantName}\""
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.height(8.dp))
                                    Text(text, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
