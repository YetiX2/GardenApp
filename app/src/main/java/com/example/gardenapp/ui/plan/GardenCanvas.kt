package com.example.gardenapp.ui.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.GardenType
import com.example.gardenapp.data.db.PlantEntity
import kotlin.math.hypot

private const val DOUBLE_TAP_TIMEOUT = 300L

@Composable
fun GardenCanvas(
    state: GardenPlanState,
    plants: List<PlantEntity>,
    childGardens: List<GardenEntity>,
    plantColor: Color,
    bedColor: Color,
    greenhouseColor: Color,
    buildingColor: Color,
    gridColor: Color,
    gardenBackgroundColor: Color,
    textColor: Color,
    selectedStrokeColor: Color,
    onPlantSelect: (PlantEntity?) -> Unit,
    onGardenSelect: (GardenEntity?) -> Unit,
    onPlantDrag: (PlantEntity) -> Unit,
    onGardenDrag: (GardenEntity) -> Unit,
    onPlantUpdate: (PlantEntity) -> Unit,
    onGardenUpdate: (GardenEntity) -> Unit,
    onGardenOpen: (GardenEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val currentPlants by rememberUpdatedState(plants)
    val currentChildGardens by rememberUpdatedState(childGardens)

    // 📌 ОДНОРАЗОВАЯ ЦЕНТРОВКА ПРИ ПЕРВОЙ ОТРИСОВКЕ
    LaunchedEffect(state.garden, state.canvasSize, state.scale) {
        state.ensureGardenCentered()
    }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLocked) {
                var lastTapTime = 0L
                var lastTapWorldPos: Offset? = null
                var lastTappedGardenId: String? = null

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

                    // ДВИГАТЬ ОБЪЕКТЫ МОЖНО ТОЛЬКО ЕСЛИ НЕ ЗАБЛОКИРОВАНО
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
                                // здесь isPanning выставим ниже, чтобы панорама работала и в lock
                            }
                        }
                    }

                    // CHANGED: панорама разрешена ВСЕГДА, если не тащим объект
                    if (!isDraggingObject) {
                        isPanning = true
                    }

                    val initialPos = down.position
                    var lastPos = down.position
                    var hasMoved = false

                    while (true) {
                        val event = awaitPointerEvent()

                        if (event.changes.all { !it.pressed }) break

                        val pressedChanges = event.changes.filter { it.pressed }

                        // PINCH-ZOOM — НЕ ЗАВИСИТ ОТ isLocked
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
                        }
                        // CHANGED: pan не проверяет isLocked
                        else if (isPanning) {
                            val pan = (pos - lastPos) / state.scale
                            state.updateViewWithConstraints(pan, 1f)
                        }

                        lastPos = pos
                        change.consume()
                    }

                    // Фиксировать перемещение объектов — только если не locked
                    if (isDraggingObject && !isTransform && !state.isLocked && hasMoved) {
                        state.selectedPlant?.let { onPlantUpdate(it) }
                        state.selectedChildGarden?.let { onGardenUpdate(it) }
                    }

                    // ДВОЙНОЙ ТАП ПО GARDEN (кроме BUILDING) — РАБОТАЕТ ДАЖЕ В LOCK
                    val tappedGarden = hitGarden
                    val isTapOnGarden =
                        tappedGarden != null &&
                                tappedGarden.type != GardenType.BUILDING &&
                                !isTransform &&
                                !hasMoved
                    // CHANGED: убрали !state.isLocked, чтобы double-tap работал в lock

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
            // Draw background first - REMOVED the alpha check
            val backgroundRect = worldToScreen(
                Rect(0f, 0f, it.widthCm.toFloat(), it.heightCm.toFloat()),
                state.scale,
                state.offset
            )
            drawRect(color = gardenBackgroundColor, topLeft = backgroundRect.topLeft, size = backgroundRect.size)
            
            if (state.snapToGrid) {
                drawGrid(
                    garden = it,
                    color = gridColor,
                    step = state.baseGridPx,
                    scale = state.scale,
                    offset = state.offset
                )
            }
        }

        childGardens.forEach { child ->
            drawChildGarden(
                garden = child,
                bedColor = bedColor,
                greenhouseColor = greenhouseColor,
                buildingColor = buildingColor,
                selectedColor = selectedStrokeColor,
                textColor = textColor,
                state = state,
                textMeasurer = textMeasurer
            )
        }

        plants.forEach { p ->
            drawPlant(
                plant = p,
                plantColor = plantColor,
                selectedColor = selectedStrokeColor,
                textColor = textColor,
                state = state,
                textMeasurer = textMeasurer
            )
        }
    }
}
