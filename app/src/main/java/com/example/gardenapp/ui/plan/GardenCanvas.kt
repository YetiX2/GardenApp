package com.example.gardenapp.ui.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.PlantEntity
import kotlin.math.hypot

@Composable
fun GardenCanvas(
    state: GardenPlanState,
    plants: List<PlantEntity>,
    childGardens: List<GardenEntity>,
    onPlantSelect: (PlantEntity?) -> Unit,
    onGardenSelect: (GardenEntity?) -> Unit,
    onPlantDrag: (PlantEntity) -> Unit, // ADDED
    onGardenDrag: (GardenEntity) -> Unit, // ADDED
    onPlantUpdate: (PlantEntity) -> Unit,
    onGardenUpdate: (GardenEntity) -> Unit,
    onGardenOpen: (GardenEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val plantColor = Color(0xFF4CAF50)
    val bedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    val greenhouseColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val buildingColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    val selectedStroke = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val currentPlants by rememberUpdatedState(plants)
    val currentChildGardens by rememberUpdatedState(childGardens)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLocked) { 
                forEachGesture {
                    awaitPointerEventScope {
                        val down = awaitFirstDown()
                        val worldPos = state.screenToWorld(down.position)

                        val hitGarden = currentChildGardens.find { it.toRect().contains(worldPos) }
                        val hitPlant = currentPlants.find {
                            hypot(it.x - worldPos.x, it.y - worldPos.y) <= it.radius + 16 / state.scale
                        }

                        if (hitGarden != null) {
                            if (!state.isLocked) {
                                onGardenSelect(hitGarden)
                                onPlantSelect(null)
                                state.dragStartOffset = worldPos - Offset((hitGarden.x ?: 0).toFloat(), (hitGarden.y ?: 0).toFloat())
                                state.dragging = true
                            }
                        } else if (hitPlant != null) {
                            if (!state.isLocked) {
                                onPlantSelect(hitPlant)
                                onGardenSelect(null)
                                state.dragStartOffset = worldPos - Offset(hitPlant.x, hitPlant.y)
                                state.dragging = true
                            }
                        } else {
                            onPlantSelect(null)
                            onGardenSelect(null)
                            state.dragging = false
                        }

                        if (state.dragging) {
                            drag(down.id) {
                                val currentPointerPos = state.screenToWorld(it.position)
                                state.selectedPlant?.let { plant ->
                                    val newCenter = currentPointerPos - state.dragStartOffset
                                    val constrainedPos = state.getConstrainedPlantPosition(newCenter, plant.radius)
                                    onPlantDrag(plant.copy(x = constrainedPos.x, y = constrainedPos.y)) // USE onPlantDrag
                                }
                                state.selectedChildGarden?.let { garden ->
                                    val newTopLeft = currentPointerPos - state.dragStartOffset
                                    val constrainedPos = state.getConstrainedGardenPosition(newTopLeft, garden.widthCm.toFloat(), garden.heightCm.toFloat())
                                    onGardenDrag(garden.copy(x = constrainedPos.x.toInt(), y = constrainedPos.y.toInt())) // USE onGardenDrag
                                }
                                it.consume()
                            }
                            // After drag ends
                            state.selectedPlant?.let { onPlantUpdate(it) }
                            state.selectedChildGarden?.let { onGardenUpdate(it) }
                            state.dragging = false
                        } else {
                             // This is a pan gesture on empty space
                            drag(down.id) {
                                val pan = (it.position - it.previousPosition) / state.scale
                                state.updateViewWithConstraints(pan, 1f)
                                it.consume()
                            }
                        }
                    }
                }
            }
    ) {
        state.canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        state.garden?.let {
            drawGrid(garden = it, color = gridColor, step = state.baseGridPx, scale = state.scale, offset = state.offset)
        }

        childGardens.forEach { child -> drawChildGarden(child, bedColor, greenhouseColor, buildingColor, selectedStroke, state) }

        plants.forEach { p ->
            val center = state.worldToScreen(Offset(p.x, p.y))
            drawCircle(color = plantColor, radius = p.radius * state.scale, center = center)
            if (p.id == state.selectedPlant?.id) {
                drawCircle(color = selectedStroke, radius = (p.radius + 6) * state.scale, center = center, style = Stroke(width = 3f))
            }
        }
    }
}
