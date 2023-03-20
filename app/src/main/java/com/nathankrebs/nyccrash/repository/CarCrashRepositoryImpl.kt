package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.db.CarCrashLocalDataSource
import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.network.CarCrashApiItem
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CarCrashRepositoryImpl(
    private val carCrashNetworkDataSource: CarCrashNetworkDataSource,
    private val carCrashLocalDataSource: CarCrashLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : CarCrashRepository {

    private var requestingData: Boolean = false

    override val carCrashes: Flow<List<CarCrashItem>> =
        carCrashLocalDataSource.carCrashes
            .map { localData ->
                // if we have no data in the local data source and we're not already fetching the
                // data, then get the data from remote and save it into the local data source. If
                // we have data from the local data source, just return that
                if (localData.isEmpty() && !requestingData) {
                    val newData = requestRemoteData()
                    withContext(ioDispatcher) {
                        carCrashLocalDataSource.saveCarCrashes(newData.map { it.toLocalModel() })
                    }
                    // now return data
                    newData
                } else {
                    localData.map { it.toModel() }
                }
            }
            .distinctUntilChanged()

    /**
     * Request the data from the remote source
     */
    private suspend fun requestRemoteData(): List<CarCrashItem> {
        requestingData = true
        return withContext(ioDispatcher) {
            carCrashNetworkDataSource.getCarCrashes()
                .mapNotNull { it.toModel() }
        }.also { requestingData = false }
    }
}


/**
 * Maps an instance of [CarCrashApiItem] to [CarCrashItem] only if all relevant fields required
 * for [CarCrashItem] are available (i.e. not null),
 */
fun CarCrashApiItem.toModel(): CarCrashItem? =
    CarCrashItem(
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

/**
 * Maps an instance of [CarCrashItem] to [CarCrashLocalItem].
 */
fun CarCrashItem.toLocalModel(): CarCrashLocalItem =
    CarCrashLocalItem(
        id = this.id,
        date = this.date,
        time = this.time,
        latitude = this.latitude,
        longitude = this.longitude,
    )
