package com.nathankrebs.nyccrash.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CarCrashLocalItem(
    @PrimaryKey val id: Int,
    @ColumnInfo val date: String,
    @ColumnInfo val time: String,
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longitude: Double,
)
