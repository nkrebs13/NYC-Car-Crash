package com.nathankrebs.nyccrash.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import com.nathankrebs.nyccrash.sdfDisplayString
import com.nathankrebs.nyccrash.sdfISO8601
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DateFormatSymbols
import java.util.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
        carCrashRepository.carCrashes
            .onEach {
                Log.d(TAG, "received")
                if (it.isFailure) {
                    _uiState.emit(_uiState.value.copy(status = UiState.UiStatus.Error))
                }
            }
            .mapNotNull { it.getOrNull() }
            .combine(
                currentVisibleRegion.debounce(currentVisibleRegionTimeBuffer)
            ) { newCarCrashes, newLayoutBounds ->
                // update this value now that we actually have our first combination of values
                currentVisibleRegionTimeBuffer = 1000

                // if we have no layout bounds, return all
                // if we have layout bounds, filter by what is visible
                when {
                    newLayoutBounds == null -> newCarCrashes
                    else -> {
                        newCarCrashes.filter {
                            newLayoutBounds.latLngBounds.contains(LatLng(it.latitude, it.longitude))
                        }
                    }
                }
            }
            .map { newCarCrashes ->
                val mostCrashes: String? = withContext(Dispatchers.IO) {
                    try {
                        carCrashRepository.getMostCommonCrashDate(idList = newCarCrashes.map { it.id })
                            ?.let { sdfISO8601.parse(it) }
                            ?.let { sdfDisplayString.format(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting the most common crash date", e)
                        null
                    }
                }
                UiState(
                    crashesByTime = getTimes(newCarCrashes),
                    dateWithMostCrashes = mostCrashes,
                    latLngs = newCarCrashes.map { LatLng(it.latitude, it.longitude) },
                    status = UiState.UiStatus.Data,
                )
            }
            .catch {
                Log.e(TAG, "error", it)
                _uiState.emit(_uiState.value.copy(status = UiState.UiStatus.Error))
            }
            .distinctUntilChanged()
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
     * Returns an IntArray where each element represents an hour. The value is the number of
     * crashes in that hour. The index of the array corresponds to the hour of the day (ie the
     * 0th index is the time between 12:00 midnight and 1:00am)
     */
    private fun getTimes(carCrashes: List<CarCrashItem>): IntArray {
        val hours = IntArray(24)
        // iterate over crashes, parse the time, and update the hour-indexed array such that each
        // hour's index is incremented for each crash
        carCrashes.map { it.time }
            .mapNotNull { timeString ->
                // will be something like "5:13" or "23:20"
                timeString.split(":").firstOrNull()?.toIntOrNull()
            }
            .forEach { hourOfCrash ->
                hours[hourOfCrash] = hours[hourOfCrash] + 1
            }
        return hours
    }

    /**
     * The UI state
     *
     * @param crashesByTime An IntArray of size 24 where each index represents an hour of the day
     * and the value represents the number of crashes in that hour. The 0th index is the 1st hour
     * of the day (12:00am - 1:00am)
     * @param latLngs The list of LatLng objects representing the location of each crash.
     * @param dateWithMostCrashes A String value for the date that has the most crashes.
     * @param status The current [UiStatus] of the data
     */
    data class UiState(
        val crashesByTime: IntArray,
        val latLngs: List<LatLng>,
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
                crashesByTime = IntArray(24),
                latLngs = emptyList(),
                dateWithMostCrashes = null,
                status = UiStatus.Loading,
            )
        }
    }

    companion object {
        private const val TAG = "CarCrashVM"
    }
}
