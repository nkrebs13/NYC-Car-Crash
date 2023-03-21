package com.nathankrebs.nyccrash.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.google.maps.android.heatmaps.HeatmapTileProvider

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
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = rememberCameraPositionState {
            position = cameraPositionState
        },
        contentDescription = null,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        content = {
            if (latLngs.isNotEmpty()) {
                val tileProvider = remember(latLngs) {
                    HeatmapTileProvider.Builder()
                        .data(latLngs)
                        .build()
                }
                TileOverlay(
                    tileProvider = tileProvider,
                    state = rememberTileOverlayState(),
                    fadeIn = true,
                    transparency = 0f,
                    visible = true,
                    onClick = {},
                )
            }
        }
    )
}
