package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.network.CarCrashApiItem
import com.nathankrebs.nyccrash.network.CarCrashNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onSubscription

class CarCrashRepositoryImpl(
    private val carCrashNetworkDataSource: CarCrashNetworkDataSource,
): CarCrashRepository {

    private val _carCrashes: MutableSharedFlow<List<CarCrashApiItem>> =
        MutableSharedFlow(replay = 1)

    /**
     * If true, then the SharedFlow does not have data in the cache
     */
    private val MutableSharedFlow<List<CarCrashApiItem>>.weDoNotHaveDataYet: Boolean
        get() = this.replayCache.firstOrNull()?.isEmpty() != false

    override val carCrashes: Flow<List<CarCrashApiItem>> = _carCrashes
        // if we don't have data yet on subscription, then request it
        .onSubscription {
            if(_carCrashes.weDoNotHaveDataYet) {
                _carCrashes.tryEmit(carCrashNetworkDataSource.getCarCrashes())
            }
        }
        .distinctUntilChanged()
}