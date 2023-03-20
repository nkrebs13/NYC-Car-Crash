package com.nathankrebs.nyccrash.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private val cameraPositionState = CameraPosition.fromLatLngZoom(
    LatLng(40.7128, -74.0060), 10f
)

private val mapUiSettings = MapUiSettings(
    compassEnabled = false,
    indoorLevelPickerEnabled = false,
    mapToolbarEnabled = true,
    myLocationButtonEnabled = false,
    rotationGesturesEnabled = false,
    scrollGesturesEnabled = false,
    scrollGesturesEnabledDuringRotateOrZoom = false,
    tiltGesturesEnabled = false,
    zoomControlsEnabled = false,
    zoomGesturesEnabled = false,
)

private val mapProperties = MapProperties(
    isMyLocationEnabled = false
)

@Composable
fun AppMap(
    modifier: Modifier = Modifier,
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = rememberCameraPositionState {
            position = cameraPositionState
        },
        contentDescription = null,
        properties = mapProperties,
        uiSettings = mapUiSettings,
    )
}
