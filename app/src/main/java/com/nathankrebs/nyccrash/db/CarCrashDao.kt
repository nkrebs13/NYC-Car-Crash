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
    suspend fun getAll(): List<CarCrashLocalItem>

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
    suspend fun getMostCommonDateForIdsInternal(ids: List<Int>): String?

    /**
     * Batching method to get the most common date among crashes with the given IDs.
     * This method processes large ID lists in batches to avoid SQLite's parameter limit,
     * and uses getMostCommonDateForIdsInternal as the internal query method.
     * Returns the date string that appears most frequently among all ids, or null if no matches found.
     */
    suspend fun getMostCommonDateForIds(ids: List<Int>): String? {
        if (ids.isEmpty()) return null
        val batchSize = 999
        val dateCounts = mutableMapOf<String, Int>()
        ids.chunked(batchSize).forEach { batch ->
            // For each batch, get all dates for the batch
            val batchDates = getAllDatesForIds(batch)
            batchDates.forEach { date ->
                dateCounts[date] = (dateCounts[date] ?: 0) + 1
            }
        }
        // Find the date with the highest count
        return dateCounts.maxByOrNull { it.value }?.key
    }

    @Query("SELECT date FROM CarCrashLocalItem WHERE id IN (:ids)")
    suspend fun getAllDatesForIds(ids: List<Int>): List<String>
    @Query("DELETE FROM CarCrashLocalItem")
    suspend fun deleteAll()

    @Query("DELETE FROM CarCrashLocalItem WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)
}
