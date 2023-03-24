package com.nathankrebs.nyccrash.db

import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import kotlinx.coroutines.flow.Flow

class CarCrashLocalDataSourceImpl(
    private val database: CarCrashDatabase
): CarCrashLocalDataSource {

    override val carCrashes: Flow<List<CarCrashLocalItem>>
        get() = database.carCrashDao().getAllContinuously()

    override fun getCarCrashes(): List<CarCrashLocalItem> {
        return database.carCrashDao().getAll()
    }

    override suspend fun saveCarCrashes(carCrashes: List<CarCrashLocalItem>) {
        database.carCrashDao().insertCarCrashes(carCrashes)
    }

    override suspend fun getLatestCarCrash(): CarCrashLocalItem {
        return database.carCrashDao().getNewestCarCrashLocalItem()
    }
}