package com.nathankrebs.nyccrash.ui.compose

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.nathankrebs.nyccrash.R

@SuppressLint("MissingPermission")
@Composable
fun AppMap(
    modifier: Modifier,
    latLngs: List<LatLng>,
    onCameraMoved: (VisibleRegion) -> Unit,
) {
    val heatmapTileProvider: MutableState<HeatmapTileProvider?> = remember {
        mutableStateOf(null)
    }

    val googleMap: MutableState<GoogleMap?> = remember {
        mutableStateOf(null)
    }

    LaunchedEffect(latLngs) {
        // the map cannot load
        if (latLngs.isEmpty()) return@LaunchedEffect

        val heatmap = heatmapTileProvider.value
        if (heatmap == null) {
            val tileProvider = HeatmapTileProvider.Builder()
                .data(latLngs)
                .build()
            heatmapTileProvider.value = tileProvider

            googleMap.value?.addTileOverlay(
                TileOverlayOptions().tileProvider(tileProvider)
            )
        } else {
            heatmap.setData(latLngs)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.crash_map, null, false)
            val mapFragment: SupportMapFragment =
                (context as FragmentActivity).supportFragmentManager
                    .findFragmentById(R.id.crashSupportMapFragment) as SupportMapFragment
            mapFragment.getMapAsync { map ->
                googleMap.value = map

                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(40.69, -73.89194))
                    .zoom(10f)
                    .build()
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                map.uiSettings.apply {
                    this.isCompassEnabled = false
                    this.isIndoorLevelPickerEnabled = false
                    this.isMapToolbarEnabled = false
                    this.isMyLocationButtonEnabled = false
                    this.isRotateGesturesEnabled = false
                    this.isTiltGesturesEnabled = false
                    this.isZoomControlsEnabled = false
                    this.isZoomGesturesEnabled = true
                }
                map.isMyLocationEnabled = false
                map.setOnCameraIdleListener {
                    onCameraMoved.invoke(map.projection.visibleRegion)
                }
            }

            view
        }
    )
}
