package com.nathankrebs.nyccrash.model

data class CarCrashItem(
    val id: Int,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
)