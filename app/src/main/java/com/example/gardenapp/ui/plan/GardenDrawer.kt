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

    val textLayoutResult = textMeasurer.measure(
        text = garden.name,
        style = TextStyle(fontSize = 14.sp, color = textColor)
    )
    //if (rect.width > textLayoutResult.size.width && rect.height > textLayoutResult.size.height) {
        drawText(
            textLayoutResult,
            topLeft = rect.center - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f)
        )
   // }
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
        //if (radius * 2 > textLayoutResult.size.width) {
            drawText(
                textLayoutResult,
                topLeft = center - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f)
            )
       // }
    }
}

internal fun worldToScreen(rect: Rect, scale: Float, offset: Offset): Rect {
    return Rect(
        topLeft = rect.topLeft * scale + offset,
        bottomRight = rect.bottomRight * scale + offset
    )
}

internal fun DrawScope.drawGrid(garden: GardenEntity, color: Color, step: Float, scale: Float, offset: Offset) {
    val scaledStep = step * scale
    if (scaledStep < 10) return

    val gardenRect = worldToScreen(Rect(0f, 0f, garden.widthCm.toFloat(), garden.heightCm.toFloat()), scale, offset)

    val startX = (-offset.x % scaledStep)
    var currentX = startX
    while (currentX < size.width) {
        if (currentX >= gardenRect.left && currentX <= gardenRect.right) {
            drawLine(color, Offset(currentX, gardenRect.top.coerceAtLeast(0f)), Offset(currentX, gardenRect.bottom.coerceAtMost(size.height)))
        }
        currentX += scaledStep
    }

    val startY = (-offset.y % scaledStep)
    var currentY = startY
    while (currentY < size.height) {
        if (currentY >= gardenRect.top && currentY <= gardenRect.bottom) {
            drawLine(color, Offset(gardenRect.left.coerceAtLeast(0f), currentY), Offset(gardenRect.right.coerceAtMost(size.width), currentY))
        }
        currentY += scaledStep
    }
}
