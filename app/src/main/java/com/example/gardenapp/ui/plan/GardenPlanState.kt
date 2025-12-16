package com.example.gardenapp.ui.plan

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.gardenapp.data.db.GardenEntity
import com.example.gardenapp.data.db.PlantEntity
import kotlinx.coroutines.CoroutineScope
import kotlin.math.round

@Composable
fun rememberGardenPlanState(
    garden: GardenEntity?,
    coroutineScope: CoroutineScope
): GardenPlanState {
    return remember(garden, coroutineScope) {
        GardenPlanState(garden, coroutineScope)
    }
}

class GardenPlanState(
    val garden: GardenEntity?,
    val scope: CoroutineScope
) {
    var scale by mutableStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var snapToGrid by mutableStateOf(true)
    var showGrid by mutableStateOf(true)
    var showNames by mutableStateOf(true) // ADDED
    var isLocked by mutableStateOf(false)
    var canvasSize by mutableStateOf(IntSize.Zero)

    var selectedPlant by mutableStateOf<PlantEntity?>(null)
    var selectedChildGarden by mutableStateOf<GardenEntity?>(null)
    
    var dragging by mutableStateOf(false)
    var dragStartOffset by mutableStateOf(Offset.Zero)

    val baseGridPx: Float get() = garden?.gridStepCm?.toFloat() ?: 50f

    var isViewportInitialized: Boolean by mutableStateOf(false)

    fun ensureGardenCentered() {
        if (isViewportInitialized) return

        val g = garden ?: return
        if (canvasSize.width == 0 || canvasSize.height == 0) return

        val gardenWidthPx = g.widthCm * scale
        val gardenHeightPx = g.heightCm * scale

        offset = Offset(
            x = (canvasSize.width - gardenWidthPx) / 2f,
            y = (canvasSize.height - gardenHeightPx) / 2f
        )

        isViewportInitialized = true
    }

    fun screenToWorld(screen: Offset): Offset = (screen - offset) / scale
    fun worldToScreen(world: Offset): Offset = world * scale + offset

    fun getConstrainedPlantPosition(worldPos: Offset, plantRadius: Float): Offset {
        val gardenWidth = garden?.widthCm?.toFloat()
        val gardenHeight = garden?.heightCm?.toFloat()

        val snapped = if (snapToGrid) {
            Offset(
                x = (round(worldPos.x / baseGridPx) * baseGridPx),
                y = (round(worldPos.y / baseGridPx) * baseGridPx)
            )
        } else {
            worldPos
        }

        if (gardenWidth != null && gardenHeight != null) {
            return Offset(
                x = snapped.x.coerceIn(plantRadius, gardenWidth - plantRadius),
                y = snapped.y.coerceIn(plantRadius, gardenHeight - plantRadius)
            )
        }
        return snapped
    }

    fun getConstrainedGardenPosition(worldPos: Offset, gardenWidth: Float, gardenHeight: Float): Offset {
        val parentWidth = garden?.widthCm?.toFloat()
        val parentHeight = garden?.heightCm?.toFloat()

        val snapped = if (snapToGrid) {
            Offset(
                x = (round(worldPos.x / baseGridPx) * baseGridPx),
                y = (round(worldPos.y / baseGridPx) * baseGridPx)
            )
        } else {
            worldPos
        }

        if (parentWidth != null && parentHeight != null) {
            return Offset(
                x = snapped.x.coerceIn(0f, parentWidth - gardenWidth),
                y = snapped.y.coerceIn(0f, parentHeight - gardenHeight)
            )
        }
        return snapped
    }

    fun updateViewWithConstraints(pan: Offset, zoom: Float) {
        scale = (scale * zoom).coerceIn(0.2f, 5f)

        val newOffset = offset + pan

        val gardenWidthScaled = (garden?.widthCm?.toFloat() ?: 0f) * scale
        val gardenHeightScaled = (garden?.heightCm?.toFloat() ?: 0f) * scale

        val marginX = canvasSize.width * 0.5f
        val marginY = canvasSize.height * 0.5f

        val minOffsetX = -gardenWidthScaled + canvasSize.width - marginX
        val maxOffsetX = marginX
        val minOffsetY = -gardenHeightScaled + canvasSize.height - marginY
        val maxOffsetY = marginY

        val tempOffset = if (gardenWidthScaled < canvasSize.width) {
            Offset(x = (canvasSize.width - gardenWidthScaled) / 2f, y = newOffset.y)
        } else {
            Offset(x = newOffset.x.coerceIn(minOffsetX, maxOffsetX), y = newOffset.y)
        }

        offset = if (gardenHeightScaled < canvasSize.height) {
            Offset(x = tempOffset.x, y = (canvasSize.height - gardenHeightScaled) / 2f)
        } else {
            Offset(x = tempOffset.x, y = newOffset.y.coerceIn(minOffsetY, maxOffsetY))
        }
    }
}
