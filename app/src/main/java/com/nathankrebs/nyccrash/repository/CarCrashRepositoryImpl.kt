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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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

    private var isFirstTimeGettingData = true

    // the car crashes are driven from the "hot" flow of the local data source
    // remote data source items are not emitted directly to simplify this reactive chain
    override val carCrashes: Flow<List<CarCrashItem>> =
        carCrashLocalDataSource.carCrashes
            // if we have no data in the local data source and we're not already fetching the
            // data, then get the data from remote and save it into the local data source.
            .onEach { localData ->
                if (isFirstTimeGettingData) {
                    isFirstTimeGettingData = false
                    if (localData.isEmpty() && !requestingData) {
                        requestRemoteData()
                    } else {
                        refreshDataIfNeeded()
                    }
                }
            }
            // we don't want to emit prematurely since we're making requests in parallel
            .filter { !requestingData }
            .map { listOfItems -> listOfItems.map { it.toModel() } }
            .distinctUntilChanged()

    override suspend fun getMostCommonCrashDate(idList: List<Int>): String {
        return withContext(ioDispatcher) {
            carCrashLocalDataSource.getMostCommonDate(idList)
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

    private suspend fun requestAndSaveDataBeteenTwoDates(startDate: String, endDate: String) {
        withContext(ioDispatcher) {
            // request crashes for a particular month
            val crashes = carCrashNetworkDataSource.getCarCrashes(
                startDate = startDate,
                endDate = endDate,
            ).mapNotNull { it.toLocalModel() }
            // now save to local data source
            Log.d(TAG, "saved ${crashes.size} crashes for dates $startDate - $endDate")
            carCrashLocalDataSource.saveCarCrashes(crashes)
        }
    }

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
