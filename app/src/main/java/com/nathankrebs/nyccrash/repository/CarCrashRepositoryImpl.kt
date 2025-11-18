package com.nathankrebs.nyccrash.repository

import android.util.Log
import com.nathankrebs.nyccrash.db.CarCrashLocalDataSource
import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.network.CarCrashApiItem
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import com.nathankrebs.nyccrash.sdfISO8601
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val repositoryScope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val requestMutex = Mutex()
    private var isSubscribedToLocalSource = false

    private val _carCrashes: MutableStateFlow<Result<List<CarCrashItem>>> =
        MutableStateFlow(Result.success(emptyList()))

    override val carCrashes: Flow<Result<List<CarCrashItem>>> = _carCrashes

    init {
        // Set up the local source listener once at initialization
        setupLocalSourceListener()
    }

    /**
     * Sets up a listener for the local data source that runs in the repository scope.
     * This ensures the Flow collection doesn't block the caller.
     */
    private fun setupLocalSourceListener() {
        if (isSubscribedToLocalSource) return
        isSubscribedToLocalSource = true

        repositoryScope.launch {
            carCrashLocalDataSource.carCrashes
                .map { listOfItems -> listOfItems.map { it.toModel() } }
                .onEach { items ->
                    Log.d(TAG, "Local source emitted ${items.size} items")
                    _carCrashes.emit(Result.success(items))
                }
                .catch { error ->
                    Log.e(TAG, "Error observing local data source", error)
                    _carCrashes.emit(Result.failure(error))
                }
                .collect { }
        }
    }

    override suspend fun requestCarCrashes() {
        // Use mutex to prevent concurrent requests
        requestMutex.withLock {
            withContext(ioDispatcher) {
                try {
                    val localCount = carCrashLocalDataSource.getCount()

                    if (localCount > 0) {
                        // We have local data, check if we need to refresh
                        refreshDataIfNeeded()
                    } else {
                        // No local data, fetch from network
                        requestRemoteData()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error requesting car crashes", e)
                    _carCrashes.emit(Result.failure(e))
                }
            }
        }
    }

    override suspend fun getMostCommonCrashDate(idList: List<Int>): String? {
        if (idList.isEmpty()) return null

        return withContext(ioDispatcher) {
            try {
                // Use efficient database query instead of loading all data into memory
                carCrashLocalDataSource.getMostCommonDateForIds(idList)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting most common crash date", e)
                null
            }
        }
    }

    /**
     * Request the data from the remote source for the last 3 months
     */
    private suspend fun requestRemoteData() {
        Log.d(TAG, "Requesting remote data for last 3 months")

        withContext(ioDispatcher) {
            // Make requests for last 3 months in parallel
            val deferredResults = (0L..2L).map { monthIndex ->
                async {
                    requestAndSaveDataBetweenTwoDates(
                        startDate = getDateTimeStringForMonthsAgo(monthIndex + 1),
                        endDate = getDateTimeStringForMonthsAgo(monthIndex)
                    )
                }
            }

            // Await all results
            deferredResults.forEach { it.await() }
        }

        // Clean up old data (older than 3 months)
        cleanupOldData()
    }

    /**
     * Requests data from [carCrashNetworkDataSource] within a given date range and saves the
     * results into [carCrashLocalDataSource].
     *
     * @param startDate The start of the date range to request the data
     * @param endDate The end of the date range to request the data
     */
    private suspend fun requestAndSaveDataBetweenTwoDates(startDate: String, endDate: String) {
        withContext(ioDispatcher) {
            try {
                // Request crashes for a particular date range
                val crashes = carCrashNetworkDataSource.getCarCrashes(
                    startDate = startDate,
                    endDate = endDate,
                ).mapNotNull { it.toLocalModel() }

                // Save to local data source
                Log.d(TAG, "Fetched ${crashes.size} crashes for dates $startDate - $endDate")
                carCrashLocalDataSource.saveCarCrashes(crashes)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data for $startDate - $endDate", e)
                throw e
            }
        }
    }

    /**
     * Refreshes data if the most recent crash in the database is older than [STALE_DATA_DAYS].
     * When refreshing, fetches data from [NUM_OF_DAYS_BEFORE_LAST_TO_GET] days before the latest
     * crash date up until today.
     */
    private suspend fun refreshDataIfNeeded() {
        withContext(ioDispatcher) {
            val latestCarCrash = carCrashLocalDataSource.getLatestCarCrash()
                ?: run {
                    // No data found, request full data set
                    Log.d(TAG, "No latest crash found, requesting full data")
                    requestRemoteData()
                    return@withContext
                }

            val latestDate: Date = try {
                sdfISO8601.parse(latestCarCrash.date) ?: run {
                    Log.e(TAG, "Could not parse date: ${latestCarCrash.date}")
                    requestRemoteData()
                    return@withContext
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date: ${latestCarCrash.date}", e)
                requestRemoteData()
                return@withContext
            }

            val numDaysBetween = Duration.between(latestDate.toInstant(), Instant.now()).toDays()
            Log.d(TAG, "Latest crash was $numDaysBetween days ago")

            // If data is recent enough, no need to refresh
            if (numDaysBetween <= STALE_DATA_DAYS) {
                Log.d(TAG, "Data is fresh (within $STALE_DATA_DAYS days), skipping refresh")
                return@withContext
            }

            Log.d(TAG, "Data is stale, refreshing...")
            requestAndSaveDataBetweenTwoDates(
                startDate = getDateTimeStringForDaysBeforeOtherDate(otherDate = latestDate),
                endDate = getDateTimeStringForMonthsAgo(0)
            )

            // Clean up old data
            cleanupOldData()
        }
    }

    /**
     * Removes crash data older than 3 months to keep the database size manageable.
     */
    private suspend fun cleanupOldData() {
        withContext(ioDispatcher) {
            try {
                val threeMonthsAgo = getDateTimeStringForMonthsAgo(3)
                carCrashLocalDataSource.deleteOlderThan(threeMonthsAgo)
                Log.d(TAG, "Cleaned up data older than $threeMonthsAgo")
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up old data", e)
            }
        }
    }

    /**
     * Returns a datetime string representing a date that is today minus [numMonthsAgo].
     */
    private fun getDateTimeStringForMonthsAgo(numMonthsAgo: Long): String =
        sdfISO8601.format(
            Date.from(ZonedDateTime.now().minusMonths(numMonthsAgo).toInstant())
        )

    /**
     * Returns a datetime string representing a date that is [NUM_OF_DAYS_BEFORE_LAST_TO_GET]
     * days before [otherDate].
     */
    private fun getDateTimeStringForDaysBeforeOtherDate(otherDate: Date): String =
        sdfISO8601.format(
            Date.from(otherDate.toInstant().minus(NUM_OF_DAYS_BEFORE_LAST_TO_GET, ChronoUnit.DAYS))
        )

    companion object {
        private const val TAG = "CarCrashRepoImpl"

        /**
         * The number of days after which the data is considered "stale" and more up-to-date data
         * should be requested. Set to 1 day so data refreshes daily.
         */
        const val STALE_DATA_DAYS = 1

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
        const val NUM_OF_DAYS_BEFORE_LAST_TO_GET = 5L
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
