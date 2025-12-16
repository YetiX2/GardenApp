package com.example.gardenapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb

object DefaultColors {
    val plantColor = 0xFF4CAF50.toInt()
    val bedColor = 0x99668B7E.toInt()
    val greenhouseColor = 0x99D1C4E9.toInt()
    val buildingColor = 0x99C2DEDC.toInt()
    val backgroundColor = 0x01000000.toInt()

    // These need a Composable context, so we'll make them functions
    @Composable
    fun gridColor() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f).toArgb()

    @Composable
    fun textColor() = MaterialTheme.colorScheme.onSurface.toArgb()

    @Composable
    fun selectedStrokeColor() = MaterialTheme.colorScheme.primary.toArgb()
}
