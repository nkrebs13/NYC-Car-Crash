package com.nathankrebs.nyccrash.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CarCrashDao {

    @Query("SELECT * FROM CarCrashLocalItem")
    fun getAllContinuously(): Flow<List<CarCrashLocalItem>>

    @Query("SELECT * FROM CarCrashLocalItem")
    fun getAll(): List<CarCrashLocalItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCarCrashes(carCrashes: List<CarCrashLocalItem>)

    @Query("SELECT * FROM CarCrashLocalItem ORDER BY date DESC LIMIT 1")
    fun getNewestCarCrashLocalItem(): CarCrashLocalItem

    @Query("SELECT date FROM CarCrashLocalItem WHERE id IN (:idList) GROUP BY date HAVING COUNT(*) = (SELECT MAX(dateCount) FROM (SELECT COUNT(*) as dateCount FROM CarCrashLocalItem WHERE id IN (:idList) GROUP BY date))")
    fun getDateWithMostCrashesForIds(idList: List<Int>): String
}