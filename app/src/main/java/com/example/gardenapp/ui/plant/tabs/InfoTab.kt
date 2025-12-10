package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity
import java.time.format.DateTimeFormatter

@Composable
fun InfoTab(plant: PlantEntity?) {
    if (plant == null) {
        Text("Загрузка...", modifier = Modifier.padding(16.dp))
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Сорт: ${plant.variety ?: "не указан"}")
            Text("Посажен: ${plant.plantedAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}")
        }
    }
}
