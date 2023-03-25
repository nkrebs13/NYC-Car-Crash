package com.nathankrebs.nyccrash.model

import com.nathankrebs.nyccrash.sdfISO8601

/**
 * An instance of a car crash
 *
 * @property id A unique identifier for the car crash
 * @property date A ISO8601 timestamp of the date of the crash. See [sdfISO8601]
 * @property time The local time of the car crash
 * @property latitude The latitude of the car crash
 * @property longitude The longitude of the car crash
 */
data class CarCrashItem(
    val id: Int,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double,
)