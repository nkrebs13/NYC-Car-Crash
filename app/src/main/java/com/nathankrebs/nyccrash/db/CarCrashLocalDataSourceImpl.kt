package com.nathankrebs.nyccrash.db

import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import kotlinx.coroutines.flow.Flow

class CarCrashLocalDataSourceImpl(
    private val database: CarCrashDatabase
) : CarCrashLocalDataSource {

    override val carCrashes: Flow<List<CarCrashLocalItem>>
        get() = database.carCrashDao().getAllContinuously()

    override suspend fun getCount(): Int {
        return database.carCrashDao().getCount()
    }

    override suspend fun getCarCrashes(): List<CarCrashLocalItem> {
        return database.carCrashDao().getAll()
    }

    override suspend fun saveCarCrashes(carCrashes: List<CarCrashLocalItem>) {
        database.carCrashDao().insertCarCrashes(carCrashes)
    }

    override suspend fun getLatestCarCrash(): CarCrashLocalItem? {
        return database.carCrashDao().getNewestCarCrashLocalItem()
    }

    override suspend fun getMostCommonDateForIds(ids: List<Int>): String? {
        if (ids.isEmpty()) return null
        return database.carCrashDao().getMostCommonDateForIds(ids)
    }

    override suspend fun deleteAll() {
        database.carCrashDao().deleteAll()
    }

    override suspend fun deleteOlderThan(beforeDate: String) {
        database.carCrashDao().deleteOlderThan(beforeDate)
    }
}
