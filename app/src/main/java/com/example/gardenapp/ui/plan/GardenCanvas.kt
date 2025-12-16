package com.example.gardenapp.ui.plan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Color
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
    onPlantOpen: (PlantEntity) -> Unit,   // 🔹 НОВЫЙ колбэк
    onGardenOpen: (GardenEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val currentPlants by rememberUpdatedState(plants)
    val currentChildGardens by rememberUpdatedState(childGardens)

    // Одноразовая центровка при первой отрисовке
    LaunchedEffect(state.garden, state.canvasSize, state.scale) {
        state.ensureGardenCentered()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLocked) {
                // 🔹 Раздельное состояние для дабл-тапа по растениям и саду
                var lastPlantTapTime = 0L
                var lastPlantTapWorldPos: Offset? = null
                var lastTappedPlantId: String? = null

                var lastGardenTapTime = 0L
                var lastGardenTapWorldPos: Offset? = null
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

                    // 🔹 ВЫБОР (select) РАБОТАЕТ ВСЕГДА, а drag — только если не locked
                    if (hitGarden != null) {
                        onGardenSelect(hitGarden)
                        onPlantSelect(null)
                        if (!state.isLocked) {
                            state.dragStartOffset = startWorld - Offset(
                                (hitGarden.x ?: 0).toFloat(),
                                (hitGarden.y ?: 0).toFloat()
                            )
                            isDraggingObject = true
                        }
                    } else if (hitPlant != null) {
                        onPlantSelect(hitPlant)
                        onGardenSelect(null)
                        if (!state.isLocked) {
                            state.dragStartOffset =
                                startWorld - Offset(hitPlant.x, hitPlant.y)
                            isDraggingObject = true
                        }
                    } else {
                        onPlantSelect(null)
                        onGardenSelect(null)
                    }

                    // Панорама разрешена всегда, если не тащим объект
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

                        // 🔹 PINCH-ZOOM — не зависит от lock
                        if (pressedChanges.size > 1 || isTransform) {
                            isTransform = true
                            val zoom = event.calculateZoom()
                            val panScreen = event.calculatePan()
                            // 🔹 ИСПРАВЛЕНО: pan передаём в мировых координатах
                            val panWorld = panScreen / state.scale
                            state.updateViewWithConstraints(panWorld, zoom)
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
                        } else if (isPanning) {
                            val panWorld = pos - lastPos
                            state.updateViewWithConstraints(panWorld, 1f)
                        }

                        lastPos = pos
                        change.consume()
                    }

                    // Фиксируем перемещение объектов — только если не locked
                    if (isDraggingObject && !isTransform && !state.isLocked && hasMoved) {
                        state.selectedPlant?.let { onPlantUpdate(it) }
                        state.selectedChildGarden?.let { onGardenUpdate(it) }
                    }

                    // ---------- TAP / DOUBLE-TAP ЛОГИКА ----------

                    // 🔹 Сначала проверяем растение
                    val tappedPlant = hitPlant
                    val isTapOnPlant =
                        tappedPlant != null &&
                                !isTransform &&
                                !hasMoved

                    if (isTapOnPlant && tappedPlant != null) {
                        val tapTime = down.uptimeMillis
                        val lastPosWorld = lastPlantTapWorldPos
                        val lastId = lastTappedPlantId

                        val isSamePlant = lastId != null && lastId == tappedPlant.id
                        val isWithinTime = tapTime - lastPlantTapTime <= DOUBLE_TAP_TIMEOUT
                        val isCloseEnough =
                            lastPosWorld != null &&
                                    (startWorld - lastPosWorld).getDistance() < 16f

                        if (isSamePlant && isWithinTime && isCloseEnough) {
                            // 🔹 ДВОЙНОЙ ТАП ПО РАСТЕНИЮ
                            onPlantOpen(tappedPlant)
                            lastPlantTapTime = 0L
                            lastPlantTapWorldPos = null
                            lastTappedPlantId = null
                        } else {
                            lastPlantTapTime = tapTime
                            lastPlantTapWorldPos = startWorld
                            lastTappedPlantId = tappedPlant.id
                        }

                        // Если уже обработали как plant tap, дальше garden не трогаем
                        return@awaitEachGesture
                    }

                    // 🔹 Потом проверяем сад (кроме BUILDING)
                    val tappedGarden = hitGarden
                    val isTapOnGarden =
                        tappedGarden != null &&
                                tappedGarden.type != GardenType.BUILDING &&
                                !isTransform &&
                                !hasMoved

                    if (isTapOnGarden && tappedGarden != null) {
                        val tapTime = down.uptimeMillis
                        val lastPosWorld = lastGardenTapWorldPos
                        val lastId = lastTappedGardenId

                        val isSameGarden = lastId != null && lastId == tappedGarden.id
                        val isWithinTime = tapTime - lastGardenTapTime <= DOUBLE_TAP_TIMEOUT
                        val isCloseEnough =
                            lastPosWorld != null &&
                                    (startWorld - lastPosWorld).getDistance() < 16f

                        if (isSameGarden && isWithinTime && isCloseEnough) {
                            onGardenOpen(tappedGarden)
                            lastGardenTapTime = 0L
                            lastGardenTapWorldPos = null
                            lastTappedGardenId = null
                        } else {
                            lastGardenTapTime = tapTime
                            lastGardenTapWorldPos = startWorld
                            lastTappedGardenId = tappedGarden.id
                        }
                    }
                }
            }
    ) {
        state.canvasSize = IntSize(size.width.toInt(), size.height.toInt())

        state.garden?.let {
            val backgroundRect = worldToScreen(
                Rect(0f, 0f, it.widthCm.toFloat(), it.heightCm.toFloat()),
                state.scale,
                state.offset
            )
            drawRect(
                color = gardenBackgroundColor,
                topLeft = backgroundRect.topLeft,
                size = backgroundRect.size
            )

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
