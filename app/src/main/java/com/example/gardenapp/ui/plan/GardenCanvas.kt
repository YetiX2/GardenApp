package com.example.gardenapp.ui.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
    onPlantDrag: (PlantEntity) -> Unit,
    onGardenDrag: (GardenEntity) -> Unit,
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

    // Актуальные данные внутри pointerInput без его пересоздания
    val currentPlants by rememberUpdatedState(plants)
    val currentChildGardens by rememberUpdatedState(childGardens)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLocked) {
                awaitEachGesture {
                    // Мы уже в AwaitPointerEventScope, дополнительный scope не нужен
                    val down = awaitFirstDown()
                    val startWorld = state.screenToWorld(down.position)

                    val currentPlants = currentPlants   // локальные ссылки на снапшоты
                    val currentChildGardens = currentChildGardens

                    val hitGarden = currentChildGardens.find { it.toRect().contains(startWorld) }
                    val hitPlant = currentPlants.find {
                        hypot(it.x - startWorld.x, it.y - startWorld.y) <=
                                it.radius + 16 / state.scale
                    }

                    var isDraggingObject = false
                    var isPanning = false
                    var isTransform = false

                    if (!state.isLocked) {
                        when {
                            hitGarden != null -> {
                                onGardenSelect(hitGarden)
                                onPlantSelect(null)
                                state.dragStartOffset = startWorld - Offset(
                                    (hitGarden.x ?: 0).toFloat(),
                                    (hitGarden.y ?: 0).toFloat()
                                )
                                isDraggingObject = true
                            }

                            hitPlant != null -> {
                                onPlantSelect(hitPlant)
                                onGardenSelect(null)
                                state.dragStartOffset =
                                    startWorld - Offset(hitPlant.x, hitPlant.y)
                                isDraggingObject = true
                            }

                            else -> {
                                onPlantSelect(null)
                                onGardenSelect(null)
                                isPanning = true
                            }
                        }
                    }

                    var lastPos = down.position

                    // Основной цикл жеста
                    while (true) {
                        val event = awaitPointerEvent()

                        // Если все отпустили пальцы — жест закончился
                        if (event.changes.all { !it.pressed }) break

                        // Сколько пальцев сейчас реально нажато
                        val pressedChanges = event.changes.filter { it.pressed }

                        // --- PINCH-ZOOM / ДВУХПАЛЬЦЕВЫЙ ЖЕСТ ---
                        if (pressedChanges.size > 1 || isTransform) {
                            isTransform = true
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            state.updateViewWithConstraints(pan, zoom)
                            event.changes.forEach { it.consume() }
                            continue
                        }

                        // --- ОДИН ПАЛЕЦ: DRAG ИЛИ PAN ---
                        val change = pressedChanges.first()
                        val pos = change.position

                        if (isDraggingObject && !state.isLocked) {
                            val currentWorld = state.screenToWorld(pos)

                            state.selectedPlant?.let { plant ->
                                val newCenter = currentWorld - state.dragStartOffset
                                val constrainedPos = state.getConstrainedPlantPosition(
                                    newCenter,
                                    plant.radius
                                )
                                onPlantDrag(
                                    plant.copy(
                                        x = constrainedPos.x,
                                        y = constrainedPos.y
                                    )
                                )
                            }

                            state.selectedChildGarden?.let { garden ->
                                val newTopLeft = currentWorld - state.dragStartOffset
                                val constrainedPos = state.getConstrainedGardenPosition(
                                    newTopLeft,
                                    garden.widthCm.toFloat(),
                                    garden.heightCm.toFloat()
                                )
                                onGardenDrag(
                                    garden.copy(
                                        x = constrainedPos.x.toInt(),
                                        y = constrainedPos.y.toInt()
                                    )
                                )
                            }
                        } else if (isPanning && !state.isLocked) {
                            val pan = (pos - lastPos) / state.scale
                            state.updateViewWithConstraints(pan, 1f)
                        }

                        lastPos = pos
                        change.consume()
                    }

                    // Если тащили объект и НЕ переходили в режим зума — зафиксировать изменения
                    if (isDraggingObject && !isTransform && !state.isLocked) {
                        state.selectedPlant?.let { onPlantUpdate(it) }
                        state.selectedChildGarden?.let { onGardenUpdate(it) }
                    }
                }
            }
    )
    {
        state.canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        state.garden?.let {
            drawGrid(
                garden = it,
                color = gridColor,
                step = state.baseGridPx,
                scale = state.scale,
                offset = state.offset
            )
        }

        childGardens.forEach { child ->
            drawChildGarden(
                child,
                bedColor,
                greenhouseColor,
                buildingColor,
                selectedStroke,
                state
            )
        }

        plants.forEach { p ->
            val center = state.worldToScreen(Offset(p.x, p.y))
            drawCircle(color = plantColor, radius = p.radius * state.scale, center = center)
            if (p.id == state.selectedPlant?.id) {
                drawCircle(
                    color = selectedStroke,
                    radius = (p.radius + 6) * state.scale,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}
