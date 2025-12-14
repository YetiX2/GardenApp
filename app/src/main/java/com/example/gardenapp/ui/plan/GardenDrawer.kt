package com.example.gardenapp.ui.plan

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.db.PlantEntity
import kotlin.math.floor

internal fun GardenEntity.toRect(): Rect {
    val left = (this.x ?: 0).toFloat()
    val top = (this.y ?: 0).toFloat()
    return Rect(left, top, left + this.widthCm, top + this.heightCm)
}

internal fun DrawScope.drawChildGarden(
    garden: GardenEntity,
    bedColor: Color,
    greenhouseColor: Color,
    buildingColor: Color,
    selectedColor: Color,
    textColor: Color,
    state: GardenPlanState,
    textMeasurer: TextMeasurer
) {
    val rect = worldToScreen(garden.toRect(), state.scale, state.offset)
    val color = when (garden.type) {
        GardenType.BED -> bedColor
        GardenType.GREENHOUSE -> greenhouseColor
        GardenType.BUILDING -> buildingColor
        else -> Color.Transparent
    }
    drawRect(color, topLeft = rect.topLeft, size = rect.size)
    drawRect(Color.Black.copy(alpha = 0.7f), topLeft = rect.topLeft, size = rect.size, style = Stroke(1f))
    if (garden.id == state.selectedChildGarden?.id) {
        drawRect(selectedColor, topLeft = rect.topLeft, size = rect.size, style = Stroke(width = 3f * state.scale))
    }

    if (state.showNames) { // ADDED CHECK
        val textLayoutResult = textMeasurer.measure(
            text = garden.name,
            style = TextStyle(fontSize = 14.sp, color = textColor)
        )
        drawText(
            textLayoutResult,
            topLeft = rect.center - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f)
        )
    }
}

internal fun DrawScope.drawPlant(
    plant: PlantEntity,
    plantColor: Color,
    selectedColor: Color,
    textColor: Color,
    state: GardenPlanState,
    textMeasurer: TextMeasurer
) {
    val center = state.worldToScreen(Offset(plant.x, plant.y))
    val radius = plant.radius * state.scale

    drawCircle(color = plantColor, radius = radius, center = center)

    if (plant.id == state.selectedPlant?.id) {
        drawCircle(
            color = selectedColor,
            radius = radius + 6f,
            center = center,
            style = Stroke(width = 3f * state.scale)
        )
    }

    if (state.showNames) { // ADDED CHECK
        val textToDraw = buildAnnotatedString {
            val hasTitle = !plant.title.isNullOrBlank()
            val hasVariety = !plant.variety.isNullOrBlank()
            if (hasTitle) {
                append(plant.title)
            }
            if(plant.title != plant.variety) {
                if (hasTitle && hasVariety) {
                    append("\n")
                }
                if (hasVariety) {
                    append(plant.variety)
                }
            }
        }

        if (textToDraw.isNotBlank()) {
            val textLayoutResult = textMeasurer.measure(
                text = textToDraw,
                style = TextStyle(fontSize = 12.sp, color = textColor)
            )
            drawText(
                textLayoutResult,
                topLeft = center - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f)
            )
        }
    }
}

internal fun worldToScreen(rect: Rect, scale: Float, offset: Offset): Rect {
    return Rect(
        topLeft = rect.topLeft * scale + offset,
        bottomRight = rect.bottomRight * scale + offset
    )
}

internal fun DrawScope.drawGrid(
    garden: GardenEntity,
    color: Color,
    step: Float,      // шаг сетки в "мировых" единицах (как и x/y, widthCm/heightCm)
    scale: Float,
    offset: Offset
) {
    val stepWorld = step
    val stepScreen = stepWorld * scale
    if (stepScreen < 10f) return   // слишком частая сетка — не рисуем

    val gardenWidthWorld = garden.widthCm.toFloat()
    val gardenHeightWorld = garden.heightCm.toFloat()

    // Видимая область в МИРОВЫХ координатах (0..gardenWidth/Height)
    val leftWorldVisible = ((0f - offset.x) / scale)
        .coerceIn(0f, gardenWidthWorld)
    val rightWorldVisible = ((size.width - offset.x) / scale)
        .coerceIn(0f, gardenWidthWorld)
    val topWorldVisible = ((0f - offset.y) / scale)
        .coerceIn(0f, gardenHeightWorld)
    val bottomWorldVisible = ((size.height - offset.y) / scale)
        .coerceIn(0f, gardenHeightWorld)

    if (leftWorldVisible >= rightWorldVisible || topWorldVisible >= bottomWorldVisible) return

    fun snapWorldStart(originWorld: Float, stepWorld: Float): Float {
        val k = floor(originWorld / stepWorld)
        return k * stepWorld
    }

    // --- Вертикальные линии (фиксируем x в мировых координатах) ---
    var xWorld = snapWorldStart(leftWorldVisible, stepWorld)
    while (xWorld <= rightWorldVisible) {
        val xScreen = xWorld * scale + offset.x

        val topScreen = topWorldVisible * scale + offset.y
        val bottomScreen = bottomWorldVisible * scale + offset.y

        drawLine(
            color = color,
            start = Offset(xScreen, topScreen.coerceAtLeast(0f)),
            end = Offset(xScreen, bottomScreen.coerceAtMost(size.height))
        )

        xWorld += stepWorld
    }

    // --- Горизонтальные линии ---
    var yWorld = snapWorldStart(topWorldVisible, stepWorld)
    while (yWorld <= bottomWorldVisible) {
        val yScreen = yWorld * scale + offset.y

        val leftScreen = leftWorldVisible * scale + offset.x
        val rightScreen = rightWorldVisible * scale + offset.x

        drawLine(
            color = color,
            start = Offset(leftScreen.coerceAtLeast(0f), yScreen),
            end = Offset(rightScreen.coerceAtMost(size.width), yScreen)
        )

        yWorld += stepWorld
    }
}


