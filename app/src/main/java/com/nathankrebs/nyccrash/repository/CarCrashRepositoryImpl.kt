package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.db.CarCrashLocalDataSource
import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.network.CarCrashApiItem
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import com.nathankrebs.nyccrash.sdfISO8601
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.Date

class CarCrashRepositoryImpl(
    private val carCrashNetworkDataSource: CarCrashNetworkDataSource,
    private val carCrashLocalDataSource: CarCrashLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : CarCrashRepository {

    private var requestingData: Boolean = false

    // the car crashes are driven from the "hot" flow of the local data source
    // remote data source items are not emitted directly to simplify this reactive chain
    override val carCrashes: Flow<List<CarCrashItem>> =
        carCrashLocalDataSource.carCrashes
            // if we have no data in the local data source and we're not already fetching the
            // data, then get the data from remote and save it into the local data source.
            .onEach { localData ->
                if (localData.isEmpty() && !requestingData) {
                    requestRemoteData()
                }
            }
            // we don't want to emit prematurely since we're making requests in parallel
            .filter { !requestingData }
            .map { listOfItems -> listOfItems.map { it.toModel() } }
            .distinctUntilChanged()

    /**
     * Request the data from the remote source
     */
    private suspend fun requestRemoteData() {
        requestingData = true
        withContext(ioDispatcher) {
            // make request for last 3 months in parallel by leveraging the async/await of
            // kotlin coroutines
            val (month0, month1, month2) = LongRange(0L, 2L).map { monthIndex ->
                async {
                    // request crashes for a particular month
                    val crashes = carCrashNetworkDataSource.getCarCrashes(
                        startDate = getDateTimeStringForMonthsAgo(monthIndex + 1),
                        endDate = getDateTimeStringForMonthsAgo(monthIndex)
                    ).mapNotNull { it.toLocalModel() }
                    // now save to local data source
                    carCrashLocalDataSource.saveCarCrashes(crashes)
                }
            }

            month0.await()
            month1.await()
            month2.await()
        }
        requestingData = false
    }

    /**
     * Returns a datetime string for representing a date that is today minus [numMonthsAgo].
     */
    private fun getDateTimeStringForMonthsAgo(numMonthsAgo: Long): String =
        sdfISO8601.format(
            Date.from(ZonedDateTime.now().minusMonths(numMonthsAgo).toInstant())
        )
}


/**
 * Maps an instance of [CarCrashApiItem] to [CarCrashLocalItem] only if all relevant fields required
 * for [CarCrashItem] are available (i.e. not null),
 */
fun CarCrashApiItem.toLocalModel(): CarCrashLocalItem? =
    CarCrashLocalItem(
        id = this.collisionId,
        date = this.crashDate,
        time = this.crashTime,
        latitude = this.latitude ?: Double.MIN_VALUE,
        longitude = this.longitude ?: Double.MIN_VALUE,
    ).takeUnless { it.latitude == Double.MIN_VALUE || it.longitude == Double.MIN_VALUE }

/**
 * Maps an instance of [CarCrashLocalItem] to [CarCrashItem].
 */
fun CarCrashLocalItem.toModel(): CarCrashItem =
    CarCrashItem(
        id = this.id,
        date = this.date,
        time = this.time,
        latitude = this.latitude,
        longitude = this.longitude,
    )
