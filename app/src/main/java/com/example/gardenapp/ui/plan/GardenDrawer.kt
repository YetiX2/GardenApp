package com.example.gardenapp.ui.plan

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.db.PlantEntity
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

internal fun GardenEntity.toRect(): Rect {
    val left = (this.x ?: 0).toFloat()
    val top = (this.y ?: 0).toFloat()
    return Rect(left, top, left + this.widthCm, top + this.heightCm)
}

internal fun DrawScope.drawChildGarden(
    garden: GardenEntity,
    hasPendingTasks: Boolean,
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

    if (hasPendingTasks) {
        val warningRadius = (rect.width.coerceAtMost(rect.height) * 0.2f).coerceAtMost(40f).coerceAtLeast(10f)
        val warningCenter = rect.topRight + Offset(-warningRadius, warningRadius)
        drawWarningSign(warningCenter, warningRadius)
    }

    if (state.showNames) {
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
    hasPendingTasks: Boolean,
    icon: Painter?,
    plantColor: Color,
    selectedColor: Color,
    textColor: Color,
    state: GardenPlanState,
    textMeasurer: TextMeasurer
) {
    val center = state.worldToScreen(Offset(plant.x, plant.y))
    val radius = plant.radius * state.scale

    if (icon != null) {
        withTransform({
            translate(left = center.x - radius, top = center.y - radius)
        }) {
            with(icon) {
                draw(size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2))
            }
        }
    } else {
        drawCircle(color = plantColor, radius = radius, center = center)
    }

    if (plant.id == state.selectedPlant?.id) {
        drawCircle(
            color = selectedColor,
            radius = radius + 6f,
            center = center,
            style = Stroke(width = 3f * state.scale)
        )
    }

    if (hasPendingTasks) {
        val warningRadius = (radius * 0.4f).coerceAtLeast(10f)
        val warningCenter = center + Offset(radius * 0.707f, -radius * 0.707f)
        drawWarningSign(warningCenter, warningRadius)
    }

    if (state.showNames) {
        val textToDraw = buildAnnotatedString {
            val hasTitle = !plant.title.isNullOrBlank()
            val hasVariety = !plant.variety.isNullOrBlank()
            if (hasTitle) {
                append(plant.title)
            }
            if (plant.title != plant.variety) {
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

private fun DrawScope.drawWarningSign(center: Offset, radius: Float) {
    val path = Path()
    val height = radius * 2
    val triangleHeight = sqrt(3.0) / 2.0 * height

    val p1 = Offset(center.x, center.y - radius)
    val p2 = Offset(center.x - height / 2, (center.y + triangleHeight / 2).toFloat())
    val p3 = Offset(center.x + height / 2, (center.y + triangleHeight / 2).toFloat())

    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.close()

    drawPath(path, Color.Yellow)

    val exclamationHeight = radius * 0.8f
    val exclamationWidth = radius * 0.2f
    val dotRadius = radius * 0.15f

    drawRect(
        color = Color.Black,
        topLeft = Offset(center.x - exclamationWidth / 2, center.y - exclamationHeight / 2),
        size = androidx.compose.ui.geometry.Size(exclamationWidth, exclamationHeight * 0.6f)
    )
    drawCircle(
        color = Color.Black,
        radius = dotRadius,
        center = Offset(center.x, center.y + exclamationHeight / 2)
    )
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
    step: Float,
    scale: Float,
    offset: Offset
) {
    val stepWorld = step
    val stepScreen = stepWorld * scale
    if (stepScreen < 10f) return

    val gardenWidthWorld = garden.widthCm.toFloat()
    val gardenHeightWorld = garden.heightCm.toFloat()

    val leftWorldVisible = ((0f - offset.x) / scale).coerceIn(0f, gardenWidthWorld)
    val rightWorldVisible = ((size.width - offset.x) / scale).coerceIn(0f, gardenWidthWorld)
    val topWorldVisible = ((0f - offset.y) / scale).coerceIn(0f, gardenHeightWorld)
    val bottomWorldVisible = ((size.height - offset.y) / scale).coerceIn(0f, gardenHeightWorld)

    if (leftWorldVisible >= rightWorldVisible || topWorldVisible >= bottomWorldVisible) return

    fun snapWorldStart(originWorld: Float, stepWorld: Float): Float {
        val k = floor(originWorld / stepWorld)
        return k * stepWorld
    }

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
