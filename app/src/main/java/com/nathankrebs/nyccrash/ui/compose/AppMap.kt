package com.nathankrebs.nyccrash.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

private val cameraPositionState = CameraPosition.fromLatLngZoom(
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

@Composable
fun AppMap(
    modifier: Modifier = Modifier,
    latLngs: List<LatLng>,
    onCameraMoved: (VisibleRegion) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = cameraPositionState
    }

    // keeps track of when the map is done moving so that the visible region callback can be invoked
    LaunchedEffect(Unit) {
        snapshotFlow { cameraPositionState.isMoving }
            .filter { isMoving -> !isMoving }
            .mapNotNull { cameraPositionState.projection?.visibleRegion }
            .onEach { onCameraMoved.invoke(it) }
            .collect()
    }

    val cameraMovingState = snapshotFlow { cameraPositionState.isMoving }
        .collectAsState(initial = false)

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentDescription = null,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        content = {
            if (latLngs.isNotEmpty()) {
                val tileProviderState = rememberTileOverlayState()
                val heatmapTileProvider = remember {
                    HeatmapTileProvider.Builder()
                        .data(latLngs)
                        .build()
                }

                LaunchedEffect(latLngs) {
                    heatmapTileProvider.setData(latLngs)
                    try {
                        tileProviderState.clearTileCache()
                    } catch (e: java.lang.IllegalStateException) {
                        // no op -- this is thrown if clearTileCache is called if the TileOverlay
                        // hasn't been set to the map but we don't get a callback when it has been
                        // set to the map
                    }
                }

                TileOverlay(
                    tileProvider = heatmapTileProvider,
                    state = tileProviderState,
                    visible = !cameraMovingState.value,
                    fadeIn = true,
                )
            }
        }
    )
}
