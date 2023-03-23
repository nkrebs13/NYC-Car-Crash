package com.nathankrebs.nyccrash.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.google.maps.android.heatmaps.Gradient
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
            if(latLngs.isNotEmpty()) {
                HeatMap(
                    uiState = TileOverlayMapUIState(),
                    latLngs = latLngs,
                )
            }
//            if (latLngs.isNotEmpty()) {
//                val tileProviderState = rememberTileOverlayState()
//                val tileProvider: MutableState<HeatmapTileProvider?> = remember {
//                    mutableStateOf(null)
//                }
//
//                LaunchedEffect(latLngs) {
//                    tileProvider.value?.apply {
//                        setData(latLngs)
//                        tileProviderState.clearTileCache()
//                    } ?: run {
//                        tileProvider.value = HeatmapTileProvider.Builder()
//                            .data(latLngs)
//                            .build()
//                    }
//                }
//
//                tileProvider.value?.let {
//                    TileOverlay(
//                        tileProvider = it,
//                        state = tileProviderState,
//                        visible = !cameraMovingState.value,
//                        fadeIn = true,
//                    )
//                }
//            }
        }
    )
}


@Composable
private fun HeatMap(
    uiState: TileOverlayMapUIState,
    latLngs: List<LatLng>,
) {
    val heatMapProvider = remember { mutableStateOf<HeatmapTileProvider?>(null) }
    val tileOverlay = remember { mutableStateOf<TileOverlay?>(null) }

    MapEffect(latLngs, uiState) { map ->
        val startPoints = floatArrayOf(0f, 0.35f, 0.7f)
        val colors = uiState.heatMapGradient.map { it.toArgb() }.toIntArray()
        val gradient = Gradient(colors, startPoints)

        if (heatMapProvider.value == null) {
            heatMapProvider.value = HeatmapTileProvider.Builder()
                .gradient(gradient)
                .radius(uiState.heatMapRadius.toInt())
                .opacity(uiState.heatMapOpacity.toDouble())
                .data(latLngs)
                .build()
            val tileOverlayOptions =
                TileOverlayOptions().tileProvider(heatMapProvider.value!!)
                    .visible(uiState.visible)
                    .transparency(uiState.transparency)
                    .fadeIn(uiState.fadeIn)
            tileOverlay.value = map.addTileOverlay(tileOverlayOptions)
        } else {
            heatMapProvider.value?.apply {
                setData(latLngs)
                setGradient(gradient)
                setRadius(uiState.heatMapRadius.toInt())
                setOpacity(uiState.heatMapOpacity.toDouble())
            }
            tileOverlay.value?.apply {
                fadeIn = uiState.fadeIn
                isVisible = uiState.visible
                transparency = uiState.transparency
            }
        }
        tileOverlay.value?.clearTileCache()
    }

    //You can also add the tile over lay for the heat map via compose function.
    //The difference between compose function and adding via map is that we get to access of clearTileCache method
    //While the compose function works but any update in tile provider will require clearing of previous cache
    //So we are using the tile overlay for heat map via adding it manually in the map.
    /*if (heatMapProvider != null) {
        TileOverlay(
            tileProvider = heatMapProvider!!,
            transparency = uiState.transparency,
            visible = uiState.visibility,
            fadeIn = uiState.fadeIn,
        )
    }*/
}

data class TileOverlayMapUIState(
    val fadeIn: Boolean = false,
    val transparency: Float = 0f,
    val visible: Boolean = true,
    val heatMapRadius: Float = 20f,
    val heatMapGradient: List<Color> = arrayListOf(Color.Red, Color.Blue, Color.Green),
    val heatMapOpacity: Float = 0.7f
)
