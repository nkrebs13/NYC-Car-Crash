package com.nathankrebs.nyccrash.network

interface CarCrashNetworkDataSource {
    /**
     * Returns the car crashes between [startDate] and [endDate]
     *
     * @param startDate The ISO-8601 representation of the start of the date range
     * @param endDate The ISO-8601 representation of the end of the date range
     */
    suspend fun getCarCrashes(startDate: String, endDate: String): List<CarCrashApiItem>
}