package com.nathankrebs.nyccrash.ui.compose

import androidx.annotation.StringRes
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
 * Represents the geographic bounding box for a region.
 * Uses half-open intervals [min, max) for latitude and longitude.
 */
private data class BoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
) {
    fun contains(lat: Double, lng: Double): Boolean =
        lat >= minLat && lat < maxLat && lng >= minLng && lng < maxLng
}

/**
 * NYC Borough detection and display based on approximate geographic boundaries.
 *
 * @property bounds The geographic bounding box for this borough
 * @property displayNameRes String resource ID for the borough's display name
 */
enum class Borough(
    private val bounds: BoundingBox,
    @StringRes val displayNameRes: Int
) {
    // Order matters for overlap resolution - more specific boroughs first
    MANHATTAN(
        bounds = BoundingBox(
            minLat = 40.70,
            maxLat = 40.88,
            minLng = -74.02,
            maxLng = -73.93
        ),
        displayNameRes = R.string.borough_manhattan
    ),
    STATEN_ISLAND(
        bounds = BoundingBox(
            minLat = 40.49,
            maxLat = 40.65,
            minLng = -74.26,
            maxLng = -74.05
        ),
        displayNameRes = R.string.borough_staten_island
    ),
    BRONX(
        bounds = BoundingBox(
            minLat = 40.88,
            maxLat = 40.92,
            minLng = -73.93,
            maxLng = -73.75
        ),
        displayNameRes = R.string.borough_bronx
    ),
    BROOKLYN(
        bounds = BoundingBox(
            minLat = 40.57,
            maxLat = 40.74,
            minLng = -74.05,
            maxLng = -73.83
        ),
        displayNameRes = R.string.borough_brooklyn
    ),
    QUEENS(
        bounds = BoundingBox(
            minLat = 40.54,
            maxLat = 40.80,
            minLng = -73.96,
            maxLng = -73.70
        ),
        displayNameRes = R.string.borough_queens
    );

    /**
     * Check if a coordinate falls within this borough's bounds.
     */
    fun contains(latLng: LatLng): Boolean =
        bounds.contains(latLng.latitude, latLng.longitude)

    companion object {
        /**
         * Detect which borough a coordinate falls within.
         * Returns null if outside NYC or not clearly within a borough.
         */
        fun fromLatLng(latLng: LatLng): Borough? =
            entries.firstOrNull { it.contains(latLng) }
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
    // Only show borough label when zoomed in enough
    if (mapCenter == null || zoomLevel < MIN_ZOOM_FOR_BOROUGH_LABEL) return

    val borough = Borough.fromLatLng(mapCenter) ?: return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colors.surface.copy(alpha = 0.9f),
        elevation = 4.dp,
    ) {
        Text(
            text = stringResource(borough.displayNameRes),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface
        )
    }
}

private const val MIN_ZOOM_FOR_BOROUGH_LABEL = 11f
