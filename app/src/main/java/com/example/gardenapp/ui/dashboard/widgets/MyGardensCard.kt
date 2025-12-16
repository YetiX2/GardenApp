package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.icon // ADDED IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyGardensCard(gardens: List<GardenEntity>, onOpenGardens: () -> Unit) {
    Column {
        Text("Мои грядки / участок", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Card(onClick = onOpenGardens) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (gardens.isEmpty()) {
                    Text("У вас пока нет садов. Нажмите, чтобы добавить.", modifier = Modifier.padding(16.dp))
                } else {
                    gardens.take(4).forEach { garden ->
                        Card(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(garden.type.icon, contentDescription = null, modifier = Modifier.size(48.dp)) // FIXED
                                Spacer(Modifier.height(8.dp))
                                Text(garden.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
