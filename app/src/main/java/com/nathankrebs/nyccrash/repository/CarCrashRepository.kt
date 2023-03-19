package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.model.CarCrashItem
import kotlinx.coroutines.flow.Flow

interface CarCrashRepository {

    val carCrashes: Flow<List<CarCrashItem>>
}