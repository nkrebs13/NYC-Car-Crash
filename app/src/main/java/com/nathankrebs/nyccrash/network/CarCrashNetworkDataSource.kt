package com.nathankrebs.nyccrash.network

interface CarCrashNetworkDataSource {
    suspend fun getCarCrashes(): List<CarCrashApiItem>
}