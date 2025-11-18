package com.nathankrebs.nyccrash.db

import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import kotlinx.coroutines.flow.Flow

/**
 * Exposes locally-sourced car crash data
 */
interface CarCrashLocalDataSource {

    /**
     * Auto-updating (aka "hot") list of [CarCrashLocalItem].
     *
     * Whenever a new [CarCrashLocalItem] is added to the database, then the newly updated list
     * of data will be published to the subscribers.
     */
    val carCrashes: Flow<List<CarCrashLocalItem>>

    /**
     * Returns the count of all car crashes in the local data source.
     */
    suspend fun getCount(): Int

    /**
     * Returns the list of all car crashes from the local data source.
     */
    suspend fun getCarCrashes(): List<CarCrashLocalItem>

    /**
     * Save a new list of car crashes to the local data source
     */
    suspend fun saveCarCrashes(carCrashes: List<CarCrashLocalItem>)

    /**
     * Get the [CarCrashLocalItem] that has the latest date (ie the crash that happened most
     * recently). Returns null if no crashes exist.
     */
    suspend fun getLatestCarCrash(): CarCrashLocalItem?

    /**
     * Get the date that appears most frequently among crashes with IDs in the provided list.
     * This is done efficiently via a database query.
     */
    suspend fun getMostCommonDateForIds(ids: List<Int>): String?

    /**
     * Delete all car crashes from the local data source.
     */
    suspend fun deleteAll()

    /**
     * Delete car crashes older than the specified date.
     */
    suspend fun deleteOlderThan(beforeDate: String)
}
