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
    private val scope: CoroutineScope
) {
    var scale by mutableStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var snapToGrid by mutableStateOf(true)
    var selectedPlant by mutableStateOf<PlantEntity?>(null)
    var dragging by mutableStateOf(false)
    var canvasSize by mutableStateOf(IntSize.Zero)

    val baseGridPx: Float
        get() = garden?.gridStepCm?.toFloat() ?: 50f

    fun screenToWorld(screen: Offset): Offset = (screen - offset) / scale
    fun worldToScreen(world: Offset): Offset = world * scale + offset

    fun getConstrainedPosition(worldPos: Offset, plantRadius: Float): Offset {
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

    fun resetView() {
        scale = 1f
        offset = Offset.Zero
    }
}
