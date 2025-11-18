package com.nathankrebs.nyccrash.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.heatmaps.WeightedLatLng
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import com.nathankrebs.nyccrash.sdfDisplayString
import com.nathankrebs.nyccrash.sdfISO8601
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarCrashViewModel(
    private val carCrashRepository: CarCrashRepository,
) : ViewModel() {

    /**
     * Keeps track of the current map visible region.
     *
     * See [onMapVisibleRegionChange]
     */
    private val currentVisibleRegion: MutableStateFlow<VisibleRegion?> = MutableStateFlow(null)

    /**
     * Debounce time for [currentVisibleRegion]. We don't want to debounce initially, so this is
     * 0 and will be set to a non-zero value in the future for subsequent emissions of
     * [currentVisibleRegion].
     */
    private var currentVisibleRegionTimeBuffer = 0L

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.INITIAL)

    /**
     * A Flow of the [UiState] of the application to be subscribed to. Updates will be published
     * as there is new data available.
     */
    val uiState: StateFlow<UiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(1_000),
            initialValue = UiState.INITIAL
        )

    init {
        // Updates the state as needed
        carCrashRepository.carCrashes
            // emit the error state if there's a failure
            .onEach {
                if (it.isFailure) {
                    _uiState.emit(_uiState.value.copy(status = UiState.UiStatus.Error))
                }
            }
            // try to get the non-null, successful value
            // if it's a failure, this will return null and be filtered by the mapNotNull
            .mapNotNull { it.getOrNull() }
            // combine the most recent visible region with the most recent car crash data
            .combine(currentVisibleRegion.debounce(currentVisibleRegionTimeBuffer)) { newCarCrashes, newLayoutBounds ->
                // update this value now that we actually have our first combination of values
                currentVisibleRegionTimeBuffer = 1000
                Pair(newCarCrashes, newLayoutBounds)
            }
            .map { (allCrashes, visibleRegion) ->
                // Filter crashes for chart/stats (visible region only)
                val crashesInRegion = getCrashesInVisibleRegion(visibleRegion, allCrashes)

                // Cluster all crashes for heatmap performance with weights
                val weightedLatLngs = clusterPointsWeighted(
                    allCrashes.map { LatLng(it.latitude, it.longitude) }
                )

                UiState(
                    crashesByTime = getTimes(crashesInRegion),
                    dateWithMostCrashes = getDateWithMostCrashes(crashesInRegion),
                    // Use ALL data for heatmap (clustered with weights for performance)
                    weightedLatLngs = weightedLatLngs,
                    // Use filtered data for stats display
                    visibleCrashCount = crashesInRegion.size,
                    status = UiState.UiStatus.Data,
                )
            }
            // catch any errors that occur
            .catch {
                Log.e(TAG, "error", it)
                _uiState.emit(_uiState.value.copy(status = UiState.UiStatus.Error))
            }
            .distinctUntilChanged()
            // update the state
            .onEach { _uiState.emit(it) }
            .launchIn(viewModelScope)

        viewModelScope.launch(context = Dispatchers.IO) {
            carCrashRepository.requestCarCrashes()
        }
    }

    /**
     * When the map UI moves, this should be invoked with the map's current [VisibleRegion].
     */
    fun onMapVisibleRegionChange(visibleRegion: VisibleRegion) {
        viewModelScope.launch {
            currentVisibleRegion.emit(visibleRegion)
        }
    }

    /**
     * Retry fetching the data from the repository
     */
    fun onClickRetryData() {
        viewModelScope.launch {
            _uiState.emit(
                _uiState.value.copy(status = UiState.UiStatus.Loading)
            )
            withContext(Dispatchers.IO) {
                carCrashRepository.requestCarCrashes()
            }
        }
    }

    /**
     * Clusters nearby points to reduce the total number of points for better heatmap performance.
     * Uses a grid-based clustering approach and returns WeightedLatLng with cluster size as weight.
     * This preserves density information - larger clusters appear "hotter" on the heatmap.
     */
    private fun clusterPointsWeighted(points: List<LatLng>): List<WeightedLatLng> {
        if (points.isEmpty()) return emptyList()

        // For small datasets, convert directly to weighted points with weight 1.0
        if (points.size <= MAX_UNCLUSTERED_POINTS) {
            return points.map { WeightedLatLng(it, 1.0) }
        }

        // Grid-based clustering: group points into cells
        val gridSize = CLUSTER_GRID_SIZE
        val clusters = mutableMapOf<Long, MutableList<LatLng>>()

        for (point in points) {
            // Calculate grid cell for this point using a single long key for better performance
            val clampedLng = point.longitude.coerceIn(-180.0, 180.0)
            val clampedLat = point.latitude.coerceIn(-90.0, 90.0)
            val gridX = ((clampedLng + 180) / gridSize).toInt()
            val gridY = ((clampedLat + 90) / gridSize).toInt()
            val key = (gridX.toLong() shl 32) or (gridY.toLong() and 0xFFFFFFFF)

            clusters.getOrPut(key) { mutableListOf() }.add(point)
        }

        // Return centroid of each cluster with cluster size as weight
        return clusters.values.map { clusterPoints ->
            val avgLat = clusterPoints.sumOf { it.latitude } / clusterPoints.size
            val avgLng = clusterPoints.sumOf { it.longitude } / clusterPoints.size
            // Use cluster size as weight - more points = higher intensity
            WeightedLatLng(LatLng(avgLat, avgLng), clusterPoints.size.toDouble())
        }
    }

    /**
     * Returns a List<Int> where each element represents an hour. The value is the number of
     * crashes in that hour. The index of the list corresponds to the hour of the day (ie the
     * 0th index is the time between 12:00 midnight and 1:00am)
     */
    private fun getTimes(carCrashes: List<CarCrashItem>): List<Int> {
        val hours = MutableList(24) { 0 }
        // iterate over crashes, parse the time, and update the hour-indexed list such that each
        // hour's index is incremented for each crash
        carCrashes.map { it.time }
            .mapNotNull { timeString ->
                // will be something like "5:13" or "23:20"
                timeString.split(":").firstOrNull()?.toIntOrNull()
            }
            .forEach { hourOfCrash ->
                hours[hourOfCrash] += 1
            }
        return hours
    }


    /**
     * Returns a list of [CarCrashItem] where the parameter [carCrashes] is filtered by
     * [visibleRegion] such that the returned list will only be crashes within that region. If
     * [visibleRegion] is null, then the parameter [carCrashes] is returned.
     */
    private fun getCrashesInVisibleRegion(
        visibleRegion: VisibleRegion?,
        carCrashes: List<CarCrashItem>
    ): List<CarCrashItem> = when {
        visibleRegion == null -> carCrashes
        else -> {
            carCrashes.filter {
                visibleRegion.latLngBounds.contains(LatLng(it.latitude, it.longitude))
            }
        }
    }

    /**
     * Returns a formatted date string of the date with the most number of [CarCrashItem.date]
     * values.
     */
    private suspend fun getDateWithMostCrashes(carCrashes: List<CarCrashItem>): String? =
        withContext(Dispatchers.IO) {
            try {
                carCrashRepository.getMostCommonCrashDate(idList = carCrashes.map { it.id })
                    ?.let { sdfISO8601.parse(it) }
                    ?.let { sdfDisplayString.format(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting the most common crash date", e)
                null
            }
        }

    /**
     * The UI state
     *
     * @param crashesByTime A List<Int> of size 24 where each index represents an hour of the day
     * and the value represents the number of crashes in that hour. The 0th index is the 1st hour
     * of the day (12:00am - 1:00am)
     * @param weightedLatLngs The list of WeightedLatLng objects for the heatmap. Each point has
     * a weight representing the number of crashes at that location (after clustering).
     * @param visibleCrashCount The number of crashes in the currently visible region.
     * @param dateWithMostCrashes A String value for the date that has the most crashes.
     * @param status The current [UiStatus] of the data
     */
    data class UiState(
        val crashesByTime: List<Int>,
        val weightedLatLngs: List<WeightedLatLng>,
        val visibleCrashCount: Int,
        val dateWithMostCrashes: String?,
        val status: UiStatus,
    ) {

        enum class UiStatus {
            Loading,
            Data,
            Error
        }

        companion object {
            val INITIAL = UiState(
                crashesByTime = List(24) { 0 },
                weightedLatLngs = emptyList(),
                visibleCrashCount = 0,
                dateWithMostCrashes = null,
                status = UiStatus.Loading,
            )
        }
    }

    companion object {
        private const val TAG = "CarCrashVM"

        /**
         * Maximum number of points before clustering kicks in
         */
        private const val MAX_UNCLUSTERED_POINTS = 5000

        /**
         * Grid size in degrees for clustering (smaller = more clusters = more points)
         * 0.005 degrees is roughly 500m which works well for NYC scale
         */
        private const val CLUSTER_GRID_SIZE = 0.005
    }
}
