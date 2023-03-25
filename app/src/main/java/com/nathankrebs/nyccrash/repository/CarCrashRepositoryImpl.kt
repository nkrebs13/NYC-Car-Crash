package com.nathankrebs.nyccrash.repository

import android.util.Log
import com.nathankrebs.nyccrash.db.CarCrashLocalDataSource
import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.network.CarCrashApiItem
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import com.nathankrebs.nyccrash.sdfISO8601
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date

class CarCrashRepositoryImpl(
    private val carCrashNetworkDataSource: CarCrashNetworkDataSource,
    private val carCrashLocalDataSource: CarCrashLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : CarCrashRepository {

    private var requestingData: Boolean = false

    private val _carCrashes: MutableSharedFlow<Result<List<CarCrashItem>>> =
        MutableSharedFlow(replay = 0, extraBufferCapacity = 1)

    override val carCrashes: Flow<Result<List<CarCrashItem>>> = _carCrashes

    override suspend fun requestCarCrashes() {
        withContext(ioDispatcher) {
            // get crashes from local data source
            val localCarCrashes = carCrashLocalDataSource.getCarCrashes()
            // if we have crashes, just set up the local data source listener and check if we
            // need to refresh the data
            if (localCarCrashes.isNotEmpty()) {
                listenForLocalSourceChanges()
                refreshDataIfNeeded()
            } else {
                try {
                    requestRemoteData()
                    listenForLocalSourceChanges()
                } catch (e: Exception) {
                    _carCrashes.emit(Result.failure(e))
                }
            }
        }
    }

    /**
     * Listens for changes from the local data source to update [_carCrashes]
     */
    private suspend fun listenForLocalSourceChanges() {
        carCrashLocalDataSource.carCrashes
            .map { listOfItems -> listOfItems.map { it.toModel() } }
            .distinctUntilChanged()
            // emit success
            .onEach { _carCrashes.emit(Result.success(it)) }
            // emit failure
            .catch {
                Log.e(TAG, "Error with car crash request", it)
                requestingData = false
                _carCrashes.emit(Result.failure(it))
            }
            .collect()
    }

    override suspend fun getMostCommonCrashDate(idList: List<Int>): String? {
        return withContext(ioDispatcher) {
            // get all car crashes
            carCrashLocalDataSource.getCarCrashes()
                // filter by IDs within idList
                .filter { idList.contains(it.id) }
                // group them by date
                .groupBy { it.date }
                // get the one that has the biggest list (ie the most instances of "date")
                .maxByOrNull { it.value.size }
                // take the first one (doesn't matter which, since we grouped them) and get the date
                ?.value?.firstOrNull()?.date
        }
    }

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
                    requestAndSaveDataBeteenTwoDates(
                        startDate = getDateTimeStringForMonthsAgo(monthIndex + 1),
                        endDate = getDateTimeStringForMonthsAgo(monthIndex)
                    )
                }
            }

            month0.await()
            month1.await()
            month2.await()
        }
        requestingData = false
    }

    /**
     * Requests data from [carCrashNetworkDataSource] within a given date range and saves the
     * results into [carCrashLocalDataSource].
     *
     * @param startDate The start of the date range to request the data
     * @param endDate The end of the date rnage to request the data
     */
    private suspend fun requestAndSaveDataBeteenTwoDates(startDate: String, endDate: String) {
        withContext(ioDispatcher) {
            // request crashes for a particular month
            val crashes = carCrashNetworkDataSource.getCarCrashes(
                startDate = startDate,
                endDate = endDate,
            ).mapNotNull { it.toLocalModel() }
            // now save to local data source
            Log.d(TAG, "Saved ${crashes.size} crashes for dates $startDate - $endDate")
            carCrashLocalDataSource.saveCarCrashes(crashes)
        }
    }

    /**
     * Requests new data if the most recent car crash in the database is more than
     * [STALE_DATA_DAYS] before today. If this is determined to be true, data is requested with
     * the date range being:
     * * startDate: [NUM_OF_DAYS_BEFORE_LAST_TO_GET] before the latest crash date
     * * endDate: Today
     */
    private suspend fun refreshDataIfNeeded() {
        withContext(ioDispatcher) {
            // is data stale?
            val latestCarCrash = carCrashLocalDataSource.getLatestCarCrash()
            val latestDate: Date = sdfISO8601.parse(latestCarCrash.date) ?: return@withContext
            val numDaysBetween = Duration.between(latestDate.toInstant(), Instant.now()).toDays()
            // if it's recent enough, then our data isn't too stale and we can just return
            if (numDaysBetween < STALE_DATA_DAYS) return@withContext

            requestAndSaveDataBeteenTwoDates(
                startDate = getDateTimeStringForDaysBeforeOtherDate(otherDate = latestDate),
                endDate = getDateTimeStringForMonthsAgo(0)
            )
        }
    }

    /**
     * Returns a datetime string for representing a date that is today minus [numMonthsAgo].
     */
    private fun getDateTimeStringForMonthsAgo(numMonthsAgo: Long): String =
        sdfISO8601.format(
            Date.from(ZonedDateTime.now().minusMonths(numMonthsAgo).toInstant())
        )

    /**
     * Returns a datetime string for representing a date that is [NUM_OF_DAYS_BEFORE_LAST_TO_GET]
     * before [otherDate].
     */
    private fun getDateTimeStringForDaysBeforeOtherDate(otherDate: Date): String =
        sdfISO8601.format(
            Date.from(otherDate.toInstant().minus(NUM_OF_DAYS_BEFORE_LAST_TO_GET, ChronoUnit.DAYS))
        )

    companion object {
        private const val TAG = "CarCrashRepoImpl"

        /**
         * The number of days after which the data is considered "stale" and more up-to-date data
         * should be requested
         */
        const val STALE_DATA_DAYS = 0

        /**
         * The number of days before the newest crash in the database to start for our request.
         * Crashes are uploaded to the API but not always right away. So if we have an entry in
         * the database for March 15, that means we may not have all of the data from March 15 or
         * the few days before that if the crash wasn't submitted to the remote source right away.
         *
         * This defines the number of days _before_ the date of the most recent crash in the
         * local data source that we should use as the start of our date range when requesting
         * fresh data.
         */
        const val NUM_OF_DAYS_BEFORE_LAST_TO_GET = 3L
    }
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
