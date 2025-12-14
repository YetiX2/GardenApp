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
import com.example.gardenapp.data.db.GardenType // NEW
import com.example.gardenapp.data.db.PlantEntity
import kotlin.math.hypot

private const val DOUBLE_TAP_TIMEOUT = 300L

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

    val currentPlants by rememberUpdatedState(plants)
    val currentChildGardens by rememberUpdatedState(childGardens)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLocked) {
                var lastTapTime = 0L
                var lastTapWorldPos: Offset? = null
                var lastTappedGardenId: String? = null // CHANGED: String?

                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startWorld = state.screenToWorld(down.position)

                    val plantsSnapshot = currentPlants
                    val childGardensSnapshot = currentChildGardens

                    val hitGarden = childGardensSnapshot.find { it.toRect().contains(startWorld) }
                    val hitPlant = plantsSnapshot.find {
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

                    val initialPos = down.position
                    var lastPos = down.position
                    var hasMoved = false

                    while (true) {
                        val event = awaitPointerEvent()

                        if (event.changes.all { !it.pressed }) break

                        val pressedChanges = event.changes.filter { it.pressed }

                        // PINCH-ZOOM
                        if (pressedChanges.size > 1 || isTransform) {
                            isTransform = true
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            state.updateViewWithConstraints(pan, zoom)
                            event.changes.forEach { it.consume() }
                            continue
                        }

                        val change = pressedChanges.first()
                        val pos = change.position

                        if (!hasMoved && (pos - initialPos).getDistance() > 8f) {
                            hasMoved = true
                        }

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

                    if (isDraggingObject && !isTransform && !state.isLocked && hasMoved) {
                        state.selectedPlant?.let { onPlantUpdate(it) }
                        state.selectedChildGarden?.let { onGardenUpdate(it) }
                    }

                    // ===== ДВОЙНОЙ ТАП ПО GARDEN, КРОМЕ BUILDING =====
                    val tappedGarden = hitGarden
                    val isTapOnGarden =
                        tappedGarden != null &&
                                tappedGarden.type != GardenType.BUILDING && // NEW: запрет на BUILDING
                                !isTransform &&
                                !hasMoved &&
                                !state.isLocked

                    if (isTapOnGarden && tappedGarden != null) {
                        val tapTime = down.uptimeMillis
                        val lastPos = lastTapWorldPos
                        val lastId = lastTappedGardenId

                        val isSameGarden = lastId != null && lastId == tappedGarden.id
                        val isWithinTime = tapTime - lastTapTime <= DOUBLE_TAP_TIMEOUT
                        val isCloseEnough =
                            lastPos != null &&
                                    (startWorld - lastPos).getDistance() < 16f

                        if (isSameGarden && isWithinTime && isCloseEnough) {
                            onGardenOpen(tappedGarden)
                            lastTapTime = 0L
                            lastTapWorldPos = null
                            lastTappedGardenId = null
                        } else {
                            lastTapTime = tapTime
                            lastTapWorldPos = startWorld
                            lastTappedGardenId = tappedGarden.id
                        }
                    }
                }
            }
    ) {
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
