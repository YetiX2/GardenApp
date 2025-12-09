package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WeatherCard() {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.WbSunny, contentDescription = "Погода", modifier = Modifier.size(64.dp), tint = Color(0xFFFFC107))
            Spacer(Modifier.width(16.dp))
            Text("23°", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Ночью возможны заморозки до -2 °C", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ForecastItem(day = "Пт", temp = "25°", icon = Icons.Outlined.WbSunny)
                    ForecastItem(day = "Сб", temp = "21°", icon = Icons.Outlined.Thermostat) // Placeholder icon
                    ForecastItem(day = "Вс", temp = "16°", icon = Icons.Outlined.Thermostat) // Placeholder icon
                }
            }
        }
    }
}

@Composable
private fun ForecastItem(day: String, temp: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(day, style = MaterialTheme.typography.bodySmall)
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(temp, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
