package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.PlantEntity
import com.example.gardenapp.data.db.ReferenceCultureEntity
import com.example.gardenapp.data.db.ReferenceTagEntity
import com.example.gardenapp.data.db.ReferenceVarietyEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoTab(
    plant: PlantEntity?,
    variety: ReferenceVarietyEntity?,
    culture: ReferenceCultureEntity?,
    tags: List<ReferenceTagEntity>
) {
    if (plant == null || variety == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Image Placeholder ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Image, contentDescription = "Изображение", tint = MaterialTheme.colorScheme.outline)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // --- Main Info ---
            culture?.let {
                Text(it.title, style = MaterialTheme.typography.headlineSmall)
            }
            Text("Сорт: ${plant.variety}", style = MaterialTheme.typography.titleMedium)
            Text("Посажен: ${plant.plantedAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            // --- Tags ---
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    tags.forEach { tag ->
                        AssistChip(onClick = { }, label = { Text(tag.key +" - "+tag.value) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Description ---
            /*variety.i18n.ru?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Justify)
            }*/

            Spacer(modifier = Modifier.weight(1f)) // Pushes ad to the bottom

            // --- Ad Block ---
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Купить семена этого сорта") },
                    supportingContent = { Text("в аффилированном магазине") },
                    trailingContent = { Icon(Icons.Outlined.Link, null) }
                )
            }
        }
    }
}
