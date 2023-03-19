package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.network.CarCrashApiItem
import kotlinx.coroutines.flow.Flow

interface CarCrashRepository {

    val carCrashes: Flow<List<CarCrashApiItem>>
}