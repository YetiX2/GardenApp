package com.example.gardenapp.ui.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.ReferenceCultureEntity
import com.example.gardenapp.data.db.ReferenceVarietyEntity
import com.example.gardenapp.ui.common.icon

@Composable
fun PlantListSheet(
    plants: List<PlantEntity>,
    varieties: List<ReferenceVarietyEntity>,
    cultures: List<ReferenceCultureEntity>,
    onPlantClick: (PlantEntity) -> Unit
) {
    val varietiesById = varieties.associateBy { it.id }
    val culturesById = cultures.associateBy { it.id }

    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = "Растения на участке",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        if (plants.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("На этом участке пока нет растений.")
            }
        } else {
            LazyColumn {
                items(plants, key = { it.id }) { plant ->
                    val cultureId = varietiesById[plant.varietyId]?.cultureId
                    val culture = cultureId?.let { culturesById[it] }

                    ListItem(
                        modifier = Modifier.clickable { onPlantClick(plant) },
                        headlineContent = { Text(plant.title) },
                        supportingContent = { Text(plant.variety ?: "Сорт не указан") },
                        leadingContent = {
                            if (culture != null) {
                                Icon(
                                    painter = culture.icon,
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(40.dp) // increased size for better visibility
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
