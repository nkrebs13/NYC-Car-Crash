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
}