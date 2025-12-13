package com.example.gardenapp.ui.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.db.PlantEntity
import kotlin.math.hypot

@Composable
fun GardenCanvas(
    state: GardenPlanState,
    plants: List<PlantEntity>,
    childGardens: List<GardenEntity>,
    onPlantSelect: (PlantEntity?) -> Unit,
    onPlantUpdate: (PlantEntity) -> Unit,
    onGardenOpen: (GardenEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val plantColor = Color(0xFF4CAF50)
    val bedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    val greenhouseColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val selectedStroke = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(modifier = modifier
        .fillMaxSize()
        .pointerInput(plants, childGardens, state.dragging) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.first()

                    if (change.pressed && !change.previousPressed) {
                        state.dragging = false
                        val worldPos = state.screenToWorld(change.position)

                        val hitGarden = childGardens.find { it.toRect().contains(worldPos) }
                        if (hitGarden != null) {
                            onGardenOpen(hitGarden)
                            continue
                        }

                        val hitPlant = plants.minByOrNull { hypot(it.x - worldPos.x, it.y - worldPos.y) - it.radius }
                        val hitOk = hitPlant != null && hypot(hitPlant.x - worldPos.x, hitPlant.y - worldPos.y) <= hitPlant.radius + 16 / state.scale

                        onPlantSelect(if (hitOk) hitPlant else null)
                        if (state.selectedPlant != null) state.dragging = true
                    }

                    if (state.dragging && state.selectedPlant != null) {
                        if (change.pressed) {
                            val world = state.screenToWorld(change.position)
                            val newPos = state.getConstrainedPosition(world, state.selectedPlant!!.radius)
                            val current = state.selectedPlant
                            if (current != null && (current.x != newPos.x || current.y != newPos.y)) {
                                onPlantUpdate(current.copy(x = newPos.x, y = newPos.y))
                            }
                        } else {
                            state.dragging = false
                        }
                    }
                }
            }
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                if (!state.dragging) {
                    state.updateViewWithConstraints(pan, zoom)
                }
            }
        }
    ) {
        state.canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        state.garden?.let {
            drawGrid(garden = it, color = gridColor, step = state.baseGridPx, scale = state.scale, offset = state.offset)
        }

        childGardens.forEach { child -> drawChildGarden(child, bedColor, greenhouseColor, state) }

        plants.forEach { p ->
            val center = state.worldToScreen(Offset(p.x, p.y))
            drawCircle(color = plantColor, radius = p.radius * state.scale, center = center)
            if (p.id == state.selectedPlant?.id) {
                drawCircle(color = selectedStroke, radius = (p.radius + 6) * state.scale, center = center, style = Stroke(width = 3f))
            }
        }
    }
}

private fun GardenEntity.toRect(): Rect {
    val left = (this.x ?: 0).toFloat()
    val top = (this.y ?: 0).toFloat()
    return Rect(left, top, left + this.widthCm, top + this.heightCm)
}

private fun DrawScope.drawChildGarden(garden: GardenEntity, bedColor: Color, greenhouseColor: Color, state: GardenPlanState) {
    val rect = worldToScreen(garden.toRect(), state.scale, state.offset)
    val color = when (garden.type) {
        GardenType.BED -> bedColor
        GardenType.GREENHOUSE -> greenhouseColor
        else -> Color.Transparent
    }
    drawRect(color, topLeft = rect.topLeft, size = rect.size)
    drawRect(Color.Black.copy(alpha = 0.7f), topLeft = rect.topLeft, size = rect.size, style = Stroke(1f))
}

private fun worldToScreen(rect: Rect, scale: Float, offset: Offset): Rect {
    return Rect(
        topLeft = rect.topLeft * scale + offset,
        bottomRight = rect.bottomRight * scale + offset
    )
}

private fun DrawScope.drawGrid(garden: GardenEntity, color: Color, step: Float, scale: Float, offset: Offset) {
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
