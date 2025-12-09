package com.example.gardenapp.ui.dashboard.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Box(modifier = Modifier.height(120.dp)) {
            Text(
                text = "Ad",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .alpha(0.7f),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Здесь могла бы быть ваша реклама", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
