package com.example.gardenapp.ui.plan

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity

@Composable
fun PlantListSheet(plants: List<PlantEntity>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Растения на участке",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (plants.isEmpty()) {
            item {
                Text(
                    text = "На этом участке пока нет растений.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(plants) { plant ->
                ListItem(
                    headlineContent = { Text(plant.title) },
                    supportingContent = { 
                        plant.variety?.let { Text(it) } 
                    }
                )
            }
        }
    }
}
