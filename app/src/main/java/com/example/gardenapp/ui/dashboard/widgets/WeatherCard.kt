package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gardenapp.data.weather.WeatherResponse
import com.example.gardenapp.ui.dashboard.WeatherUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherCard(state: WeatherUiState, onRetry: () -> Unit) {
    Card {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            when (state) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is WeatherUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onRetry) { Text("Повторить") }
                    }
                }
                is WeatherUiState.Success -> {
                    WeatherSuccessContent(data = state.data)
                }
            }
        }
    }
}

@Composable
private fun WeatherSuccessContent(data: WeatherResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https:${data.current.condition.iconUrl}",
            contentDescription = data.current.condition.text,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text("${data.current.tempC.roundToInt()}°", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Light))
        Spacer(Modifier.width(16.dp))
        Column {
            // TODO: Use real frost warnings based on forecast data
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                data.forecast.forecastDay.forEach {
                    ForecastItem(forecastDay = it)
                }
            }
        }
    }
}

@Composable
private fun ForecastItem(forecastDay: com.example.gardenapp.data.weather.ForecastDay) {
    val date = LocalDate.parse(forecastDay.date)
    val formatter = DateTimeFormatter.ofPattern("E", Locale("ru"))
    val dayOfWeek = date.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(dayOfWeek, style = MaterialTheme.typography.bodySmall)
        AsyncImage(
            model = "https:${forecastDay.day.condition.iconUrl}",
            contentDescription = forecastDay.day.condition.text,
            modifier = Modifier.size(32.dp)
        )
        Text("${forecastDay.day.maxTempC.roundToInt()}°", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
