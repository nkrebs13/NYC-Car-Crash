package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.model.CarCrashItem
import com.nathankrebs.nyccrash.network.CarCrashApiItem
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onSubscription

class CarCrashRepositoryImpl(
    private val carCrashNetworkDataSource: CarCrashNetworkDataSource,
) : CarCrashRepository {

    private val _carCrashes: MutableSharedFlow<List<CarCrashItem>> =
        MutableSharedFlow(replay = 1)

    /**
     * If true, then the SharedFlow does not have data in the cache
     */
    private val MutableSharedFlow<List<CarCrashItem>>.weDoNotHaveDataYet: Boolean
        get() = this.replayCache.firstOrNull()?.isEmpty() != false

    override val carCrashes: Flow<List<CarCrashItem>> = _carCrashes
        // if we don't have data yet on subscription, then request it
        .onSubscription {
            if (_carCrashes.weDoNotHaveDataYet) {
                val newCrashes: List<CarCrashItem> = carCrashNetworkDataSource.getCarCrashes()
                    .mapNotNull { it.toModel() }
                _carCrashes.tryEmit(newCrashes)
            }
        }
        .distinctUntilChanged()
}

/**
 * Maps an instance of [CarCrashApiItem] to [CarCrashItem] only if all relevant fields required
 * for [CarCrashItem] are available (i.e. not null),
 */
fun CarCrashApiItem.toModel(): CarCrashItem? =
    CarCrashItem(
        date = this.crashDate,
        time = this.crashTime,
        latitude = this.latitude ?: Double.MIN_VALUE,
        longitude = this.longitude ?: Double.MIN_VALUE,
    ).takeUnless { it.latitude == Double.MIN_VALUE || it.longitude == Double.MIN_VALUE }
