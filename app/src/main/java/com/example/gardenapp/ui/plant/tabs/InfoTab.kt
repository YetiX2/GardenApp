package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gardenapp.data.db.MonthRangeEntity
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // --- Main Info ---
            culture?.let {
                Text(it.title, style = MaterialTheme.typography.headlineSmall)
            }
            Text("Сорт: ${plant.variety}", style = MaterialTheme.typography.titleMedium)
            Text("Посажен: ${plant.plantedAt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}", style = MaterialTheme.typography.bodySmall)

            // --- Timeline Section ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                variety.bloomWindow?.let {
                    Timeline(label = "Цветение", range = it, color = MaterialTheme.colorScheme.primary)
                }
                variety.harvestWindow?.let {
                    Timeline(label = "Сбор урожая", range = it, color = MaterialTheme.colorScheme.secondary)
                }
                variety.plantingWindow?.let { planting ->
                    planting.spring?.let {
                        Timeline(label = "Посадка (весна)", range = it, color = MaterialTheme.colorScheme.tertiary)
                    }
                    planting.autumn?.let {
                        Timeline(label = "Посадка (осень)", range = it, color = MaterialTheme.colorScheme.tertiary)
                    }
                    planting.seedling?.let {
                        Timeline(label = "Посадка (рассада)", range = it, color = MaterialTheme.colorScheme.tertiary.copy(alpha=0.5f))
                    }
                    planting.transplantGreenhouse?.let {
                        Timeline(label = "Высадка (теплица)", range = it, color = MaterialTheme.colorScheme.tertiary)
                    }
                    planting.transplantOg?.let {
                        Timeline(label = "Высадка (открытый грунт)", range = it, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            // --- Tags ---
            if (tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    tags.forEach { tag ->
                        AssistChip(onClick = { }, label = { Text(tag.key +" - "+tag.value) }) // todo: make it more readable
                    }
                }
            }

            // --- Description ---
            variety.i18n.ru.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Justify)
            }

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

val months = listOf("Я", "Ф", "М", "А", "М", "И", "И", "А", "С", "О", "Н", "Д")

@Composable
fun Timeline(
    label: String,
    range: MonthRangeEntity?,
    color: Color = MaterialTheme.colorScheme.primary
) {
    if (range?.start == null || range.end == null) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            months.forEachIndexed { index, month ->
                val monthNum = index + 1
                val isInRange = monthNum >= range.start!! && monthNum <= range.end!!
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .background(
                            if (isInRange) color.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp) // Make corners rounded
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(month, fontSize = 10.sp)
                }
            }
        }
    }
}
