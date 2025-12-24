package com.example.gardenapp.ui.plant.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.CareRuleEntity
import com.example.gardenapp.data.db.TaskType
import com.example.gardenapp.data.db.icon
import com.example.gardenapp.ui.plant.dialogs.AddCareRuleDialog
import java.time.format.DateTimeFormatter

private fun TaskType.toRussian(): String = when (this) {
    TaskType.FERTILIZE -> "Подкормить"
    TaskType.PRUNE -> "Обрезать"
    TaskType.TREAT -> "Обработать"
    TaskType.WATER -> "Полить"
    TaskType.HARVEST -> "Сбор урожая"
    TaskType.OTHER -> "Другое"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareRulesTab(
    rules: List<CareRuleEntity>,
    onAddRule: (TaskType, Int, String?) -> Unit, // Renamed for clarity
    onUpdateRule: (CareRuleEntity) -> Unit, // ADDED
    onDeleteRule: (CareRuleEntity) -> Unit // Renamed for clarity
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<CareRuleEntity?>(null) }

    Scaffold(
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, null) } }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Video Blocks Section ---
            Text("Полезные видео", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { // Placeholder for 3 videos
                    VideoCard(title = "Как правильно поливать томаты")
                }
            }

            Divider()

            // --- Care Rules List ---
            Text("Правила ухода", style = MaterialTheme.typography.titleMedium)

            LazyColumn(modifier = Modifier.weight(1f)) {
                 if (rules.isEmpty()) {
                    item { Text("Правил ухода пока нет. Нажмите +, чтобы добавить.") }
                } else {
                    items(rules, key = { it.id }) { rule ->
                        val everyText = rule.everyDays?.let { "каждые $it дней" } ?: ""
                        ListItem(
                            leadingContent = { // ADDED
                                Icon(rule.type.icon, contentDescription = null)
                            },
                            headlineContent = { Text("${rule.type.toRussian()} $everyText") },
                            supportingContent = {
                                Column {
                                    Text("Начиная с ${rule.start.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                                    rule.note?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { ruleToEdit = rule }) { Icon(Icons.Default.Edit, null) }
                                    IconButton(onClick = { onDeleteRule(rule) }) { Icon(Icons.Default.Delete, null) }
                                }
                            }
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Видео-каналы по уходу за садом") },
                    supportingContent = { Text("на YouTube / RuTube") },
                    trailingContent = { Icon(Icons.Outlined.Link, null) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer for padding at the bottom
        }
    }

    if (showAddDialog) {
        AddCareRuleDialog(
            onDismiss = { showAddDialog = false },
            onAddRule = { type, days, note -> // UPDATED
                onAddRule(type, days, note)
                showAddDialog = false
            }
        )
    }

    ruleToEdit?.let { rule ->
        AddCareRuleDialog(
            initialRule = rule,
            onDismiss = { ruleToEdit = null },
            onAddRule = { type, days, note -> // UPDATED
                onUpdateRule(rule.copy(type = type, everyDays = days, note = note))
                ruleToEdit = null
            }
        )
    }
}

@Composable
private fun VideoCard(title: String) {
    Card(onClick = { /* TODO: Open video link */ }) {
        Column(modifier = Modifier.width(200.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Text(
                text = title,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
