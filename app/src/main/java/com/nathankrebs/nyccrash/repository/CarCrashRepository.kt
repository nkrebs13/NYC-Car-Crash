package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.model.CarCrashItem
import kotlinx.coroutines.flow.Flow

interface CarCrashRepository {

    val carCrashes: Flow<List<CarCrashItem>>

    /**
     * Returns a ISO8601 timestamp of the date that has the most crashes from the subset of data
     * with a [CarCrashItem.id] property contained within [idList]
     */
    suspend fun getMostCommonCrashDate(idList: List<Int>): String
}