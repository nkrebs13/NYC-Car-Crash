package com.nathankrebs.nyccrash.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nathankrebs.nyccrash.db.entity.CarCrashLocalItem

@Database(entities = [CarCrashLocalItem::class], version = 1)
abstract class CarCrashDatabase : RoomDatabase() {
    abstract fun carCrashDao(): CarCrashDao
}