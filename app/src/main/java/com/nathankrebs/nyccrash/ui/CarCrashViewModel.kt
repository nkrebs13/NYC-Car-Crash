package com.nathankrebs.nyccrash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.VisibleRegion
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.repository.CarCrashRepository
import com.nathankrebs.nyccrash.sdfISO8601
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DateFormatSymbols
import java.util.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale.filter

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

    /**
     * A Flow of the [UiState] of the application to be subscribed to. Updates will be published
     * as there is new data available.
     */
    val uiState: StateFlow<UiState> =
        carCrashRepository.carCrashes
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
            }.map { newCarCrashes ->
                UiState(
                    crashesByTime = getTimes(newCarCrashes),
                    monthWithMostCrashes = getMostDangerousMonth(newCarCrashes),
                    latLngs = newCarCrashes.map { LatLng(it.latitude, it.longitude) },
                    status = UiState.UiStatus.Data,
                )
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(1_000),
                initialValue = UiState.INITIAL
            )

    /**
     * When the map UI moves, this should be invoked with the map's current [VisibleRegion].
     */
    fun onMapVisibleRegionChange(visibleRegion: VisibleRegion) {
        viewModelScope.launch {
            currentVisibleRegion.emit(visibleRegion)
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
     * Returns a String value of the name of the month with the most number of crashes for
     * parameter [carCrashes].
     */
    private fun getMostDangerousMonth(carCrashes: List<CarCrashItem>): String {
        val months = IntArray(12)
        // map date string (ISO8601) to Date, iterate, and then populate array indexed by month
        carCrashes.mapNotNull { sdfISO8601.parse(it.date) }
            .forEach { date ->
                val calendar = Calendar.getInstance()
                calendar.time = date
                val monthIndex = calendar.get(Calendar.MONTH)
                months[monthIndex] = months[monthIndex] + 1
            }
        // use the index of the max of the array to get the month string
        return DateFormatSymbols(Locale.getDefault()).months[months.indexOf(months.max())]
    }

    /**
     * The UI state
     *
     * @param crashesByTime An IntArray of size 24 where each index represents an hour of the day
     * and the value represents the number of crashes in that hour. The 0th index is the 1st hour
     * of the day (12:00am - 1:00am)
     * @param latLngs The list of LatLng objects representing the location of each crash.
     * @param monthWithMostCrashes A String value for the month that has the most crashes. This
     * value will be null if [latLngs] is empty.
     * @param status The current [UiStatus] of the data
     */
    data class UiState(
        val crashesByTime: IntArray,
        val latLngs: List<LatLng>,
        val monthWithMostCrashes: String?,
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
                monthWithMostCrashes = null,
                status = UiStatus.Loading,
            )
        }
    }
}
