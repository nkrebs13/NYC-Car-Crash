package com.nathankrebs.nyccrash.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.nathankrebs.nyccrash.R

/**
 * NYC Borough detection and display based on approximate geographic boundaries.
 */
enum class Borough {
    MANHATTAN,
    BROOKLYN,
    QUEENS,
    BRONX,
    STATEN_ISLAND;

    companion object {
        /**
         * Detect which borough a coordinate falls within.
         * Uses simplified bounding boxes for each borough.
         * Returns null if outside NYC or not clearly within a borough.
         */
        fun fromLatLng(latLng: LatLng): Borough? {
            val lat = latLng.latitude
            val lng = latLng.longitude

            return when {
                // Manhattan - narrow island
                lat in 40.70..40.88 && lng in -74.02..-73.93 -> MANHATTAN

                // Staten Island - southwest, separate island
                lat in 40.49..40.65 && lng in -74.26..-74.05 -> STATEN_ISLAND

                // Bronx - north of Manhattan
                lat in 40.79..40.92 && lng in -73.93..-73.75 -> BRONX

                // Brooklyn - south of Queens, west of JFK
                lat in 40.57..40.74 && lng in -74.05..-73.83 -> BROOKLYN

                // Queens - east side, includes JFK/LGA
                lat in 40.54..40.80 && lng in -73.96..-73.70 -> QUEENS

                else -> null
            }
        }
    }
}

/**
 * Displays the current borough name when the map is zoomed into a specific area.
 */
@Composable
fun BoroughLabel(
    mapCenter: LatLng?,
    zoomLevel: Float,
    modifier: Modifier = Modifier
) {
    // Only show borough label when zoomed in enough (zoom > 11)
    if (mapCenter == null || zoomLevel < 11f) return

    val borough = Borough.fromLatLng(mapCenter) ?: return

    val boroughName = when (borough) {
        Borough.MANHATTAN -> stringResource(R.string.borough_manhattan)
        Borough.BROOKLYN -> stringResource(R.string.borough_brooklyn)
        Borough.QUEENS -> stringResource(R.string.borough_queens)
        Borough.BRONX -> stringResource(R.string.borough_bronx)
        Borough.STATEN_ISLAND -> stringResource(R.string.borough_staten_island)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colors.surface.copy(alpha = 0.9f),
        elevation = 4.dp,
    ) {
        Text(
            text = boroughName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface
        )
    }
}
