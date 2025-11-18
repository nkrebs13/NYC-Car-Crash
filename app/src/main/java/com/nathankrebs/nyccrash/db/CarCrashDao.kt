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

    @Query("SELECT COUNT(*) FROM CarCrashLocalItem")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarCrashes(carCrashes: List<CarCrashLocalItem>)

    @Query("SELECT * FROM CarCrashLocalItem ORDER BY date DESC LIMIT 1")
    suspend fun getNewestCarCrashLocalItem(): CarCrashLocalItem?

    /**
     * Get the date that appears most frequently among crashes with IDs in the provided list.
     * Returns the date string or null if no matches found.
     */
    @Query("""
        SELECT date FROM CarCrashLocalItem
        WHERE id IN (:ids)
        GROUP BY date
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """)
    suspend fun getMostCommonDateForIds(ids: List<Int>): String?

    @Query("DELETE FROM CarCrashLocalItem")
    suspend fun deleteAll()

    @Query("DELETE FROM CarCrashLocalItem WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)
}
