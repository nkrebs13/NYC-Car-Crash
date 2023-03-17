package com.nathankrebs.nyccrash

interface CarCrashApi {
    suspend fun getCarCrashes(): List<CarCrashItem>
}