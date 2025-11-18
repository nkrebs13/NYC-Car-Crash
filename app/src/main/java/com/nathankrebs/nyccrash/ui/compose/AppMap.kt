package com.nathankrebs.nyccrash.ui.compose

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

private val defaultCameraPosition = CameraPosition.fromLatLngZoom(
    LatLng(40.69, -73.89194), 10f
)

private val mapUiSettings = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = true,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    scrollGesturesEnabled = true,
    scrollGesturesEnabledDuringRotateOrZoom = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = false,
    zoomGesturesEnabled = true,
)

private val mapProperties = MapProperties(
    isMyLocationEnabled = false,
    mapType = MapType.NORMAL
)

// Optimized gradient with fewer color stops for faster rendering
private val heatmapGradient = Gradient(
    intArrayOf(
        Color.rgb(0, 255, 0),    // Green (low)
        Color.rgb(255, 255, 0),  // Yellow
        Color.rgb(255, 128, 0),  // Orange
        Color.rgb(255, 0, 0)     // Red (high)
    ),
    floatArrayOf(0.2f, 0.5f, 0.8f, 1.0f)
)

/**
 * Data class representing camera position info for UI purposes
 */
data class CameraInfo(
    val center: LatLng,
    val zoom: Float
)

@Composable
fun AppMap(
    modifier: Modifier = Modifier,
    weightedLatLngs: List<WeightedLatLng>,
    onCameraMoved: (VisibleRegion) -> Unit,
    onCameraPositionChanged: (CameraInfo) -> Unit = {},
) {
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    // Debounce camera movement callbacks to reduce processing
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving }
            .debounce(150) // Increased debounce for better batching
            .filter { isMoving -> !isMoving }
            .mapNotNull { cameraPositionState.projection?.visibleRegion }
            .collectLatest { onCameraMoved(it) }
    }

    // Track and report camera position changes
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.position }
            .debounce(150)
            .collectLatest { position ->
                onCameraPositionChanged(CameraInfo(position.target, position.zoom))
            }
    }

    // Track if camera is currently moving (for hiding overlay during movement)
    val isCameraMoving by snapshotFlow { cameraPositionState.isMoving }
        .collectAsState(initial = false)

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentDescription = null,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        content = {
            if (weightedLatLngs.isNotEmpty()) {
                HeatmapOverlay(
                    weightedLatLngs = weightedLatLngs,
                    isMoving = isCameraMoving
                )
            }
        }
    )
}

/**
 * Optimized heatmap overlay that minimizes tile regeneration.
 * Uses WeightedLatLng for better density representation.
 */
@Composable
private fun HeatmapOverlay(
    weightedLatLngs: List<WeightedLatLng>,
    isMoving: Boolean,
) {
    val tileOverlayState = rememberTileOverlayState()

    // Create HeatmapTileProvider with weighted data and optimized settings
    val dataSize = weightedLatLngs.size
    val heatmapTileProvider = remember(weightedLatLngs) {
        HeatmapTileProvider.Builder()
            .weightedData(weightedLatLngs)
            .radius(HEATMAP_RADIUS)
            .opacity(HEATMAP_OPACITY)
            .gradient(heatmapGradient)
            .build()
    }

    // Only clear cache when data actually changes
    LaunchedEffect(dataSize) {
        try {
            tileOverlayState.clearTileCache()
        } catch (e: IllegalStateException) {
            // Overlay not yet attached to map, ignore
        }
    }

    TileOverlay(
        tileProvider = heatmapTileProvider,
        state = tileOverlayState,
        // Hide during movement for better performance
        visible = !isMoving,
        fadeIn = true,
        transparency = 0f,
    )
}

private const val HEATMAP_RADIUS = 25 // Slightly larger radius for smoother appearance
private const val HEATMAP_OPACITY = 0.75
