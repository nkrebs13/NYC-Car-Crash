package com.nathankrebs.nyccrash.network

interface CarCrashApi {
    suspend fun getCarCrashes(): List<CarCrashApiItem>
}