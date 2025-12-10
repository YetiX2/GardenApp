package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.ReferenceTagEntity
import com.example.gardenapp.data.db.ReferenceVarietyEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoTab(plant: PlantEntity?, variety: ReferenceVarietyEntity?, tags: List<ReferenceTagEntity>) {
    if (plant == null || variety == null) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Сорт: ${plant.variety}", style = MaterialTheme.typography.titleMedium)
            Text("Посажен: ${plant.plantedAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            if (tags.isNotEmpty()) {
                Text("Тэги:", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.forEach { tag ->
                        AssistChip(onClick = { }, label = { Text(tag.value) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            variety.i18n.ru?.let {
                Text("Описание:", style = MaterialTheme.typography.titleSmall)
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
