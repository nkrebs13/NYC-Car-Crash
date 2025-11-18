package com.nathankrebs.nyccrash.ui.compose

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
import com.google.maps.android.heatmaps.HeatmapTileProvider
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
    latLngs: List<LatLng>,
    onCameraMoved: (VisibleRegion) -> Unit,
    onCameraPositionChanged: (CameraInfo) -> Unit = {},
) {
    val cameraPositionState = rememberCameraPositionState {
        position = defaultCameraPosition
    }

    // Debounce camera movement callbacks to reduce processing
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving }
            .filter { isMoving -> !isMoving }
            .debounce(100) // Small debounce to batch rapid movements
            .mapNotNull { cameraPositionState.projection?.visibleRegion }
            .collectLatest { onCameraMoved.invoke(it) }
    }

    // Track and report camera position changes
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.position }
            .debounce(100)
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
            if (latLngs.isNotEmpty()) {
                HeatmapOverlay(
                    latLngs = latLngs,
                    isMoving = isCameraMoving
                )
            }
        }
    )
}

/**
 * Optimized heatmap overlay that minimizes tile regeneration.
 */
@Composable
private fun HeatmapOverlay(
    latLngs: List<LatLng>,
    isMoving: Boolean,
) {
    val tileOverlayState = rememberTileOverlayState()

    // Create and remember the HeatmapTileProvider with optimized settings
    val heatmapTileProvider = remember(latLngs) {
        HeatmapTileProvider.Builder()
            .data(latLngs)
            .radius(HEATMAP_RADIUS)
            .opacity(HEATMAP_OPACITY)
            .build()
    }

    // Only update provider when data actually changes (not on camera move)
    LaunchedEffect(latLngs) {
        // The provider is recreated with new data via remember(latLngs)
        // Only clear cache if the overlay is already attached
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

private const val HEATMAP_RADIUS = 20
private const val HEATMAP_OPACITY = 0.7
