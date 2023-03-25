package com.nathankrebs.nyccrash.repository

import com.nathankrebs.nyccrash.model.CarCrashItem
import kotlinx.coroutines.flow.Flow

interface CarCrashRepository {

    /**
     * A Flow of a list of [CarCrashItem] representing the latest list of [CarCrashItem]
     * available. It is required that [requestCarCrashes] is invoked for this to become active
     */
    val carCrashes: Flow<Result<List<CarCrashItem>>>

    /**
     * Initializer function to start the [carCrashes] Flow
     */
    suspend fun requestCarCrashes()

    /**
     * Returns an ISO8601 representation of a date that is in the most instances of
     * [CarCrashItem.date] where the list of [CarCrashItem] is filtered to only
     * instances with IDs contained within [idList].
     *
     * In other words: given a list of IDs for [CarCrashItem], what is the date that had the
     * most number of crashes?
     */
    suspend fun getMostCommonCrashDate(idList: List<Int>): String?
}